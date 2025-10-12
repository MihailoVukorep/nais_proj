from influxdb_client import InfluxDBClient, Point
from influxdb_client.client.write_api import SYNCHRONOUS
from datetime import datetime
import logging
from app.config.settings import settings
from typing import List, Optional

logger = logging.getLogger(__name__)


class InfluxDBService:
    """Servis za rad sa InfluxDB bazom podataka"""
    
    def __init__(self):
        """Inicijalizacija InfluxDB klijenta"""
        self.client = InfluxDBClient(
            url=settings.INFLUXDB_URL,
            token=settings.INFLUXDB_TOKEN,
            org=settings.INFLUXDB_ORG
        )
        self.write_api = self.client.write_api(write_options=SYNCHRONOUS)
        self.query_api = self.client.query_api()
        self.bucket = settings.INFLUXDB_BUCKET
        self.org = settings.INFLUXDB_ORG
    
    def write_dogadjaj(
        self, 
        tip_dogadjaja: str, 
        status: str, 
        entitet_id: int, 
        iznos: float, 
        opis: str,
        timestamp: Optional[datetime] = None
    ):
        """
        Upisuje događaj u InfluxDB
        
        Args:
            tip_dogadjaja: "transakcija" ili "penal"
            status: Status događaja
            entitet_id: ID faktore ili ugovora
            iznos: Iznos u RSD
            opis: Opis događaja
            timestamp: Vremenski pečat (opciono)
        """
        try:
            point = Point("dogadjaji") \
                .tag("tip_dogadjaja", tip_dogadjaja) \
                .tag("status", status) \
                .tag("entitet_id", str(entitet_id)) \
                .field("iznos", float(iznos)) \
                .field("opis", opis)
            
            if timestamp:
                point = point.time(timestamp)
            
            self.write_api.write(bucket=self.bucket, org=self.org, record=point)
            logger.info(f"Uspešno upisan događaj: {tip_dogadjaja} - {entitet_id}")
            return True
        except Exception as e:
            logger.error(f"Greška pri upisu događaja: {str(e)}")
            raise
    
    def delete_dogadjaji_u_opsegu(self, start: datetime, end: datetime) -> bool:
        """
        Briše događaje u vremenskom opsegu
        
        Args:
            start: Početak vremenskog opsega
            end: Kraj vremenskog opsega
        """
        try:
            delete_api = self.client.delete_api()
            delete_api.delete(
                start=start,
                stop=end,
                predicate='_measurement="dogadjaji"',
                bucket=self.bucket,
                org=self.org
            )
            logger.info(f"Uspešno obrisani događaji od {start} do {end}")
            return True
        except Exception as e:
            logger.error(f"Greška pri brisanju događaja: {str(e)}")
            raise
    
    def get_dogadjaji_by_type(self, tip_dogadjaja: str, limit: int = 100) -> List[dict]:
        """
        Vraća događaje po tipu
        
        Args:
            tip_dogadjaja: "transakcija" ili "penal"
            limit: Maksimalan broj rezultata
        """
        flux_query = f'''
from(bucket: "{self.bucket}")
  |> range(start: -1y)
  |> filter(fn: (r) => r._measurement == "dogadjaji")
  |> filter(fn: (r) => r.tip_dogadjaja == "{tip_dogadjaja}")
  |> filter(fn: (r) => r._field == "iznos" or r._field == "opis")
  |> pivot(rowKey: ["_time", "entitet_id", "status"], columnKey: ["_field"], valueColumn: "_value")
  |> sort(columns: ["_time"], desc: true)
  |> limit(n: {limit})
  |> yield(name: "dogadjaji")
        '''
        
        try:
            result = self.query_api.query(flux_query, org=self.org)
            data = []
            for table in result:
                for record in table.records:
                    data.append({
                        "timestamp": record.get_time(),
                        "tip_dogadjaja": tip_dogadjaja,
                        "entitet_id": int(record.values.get('entitet_id')),
                        "status": record.values.get('status'),
                        "iznos": float(record.values.get('iznos', 0)),
                        "opis": str(record.values.get('opis', ''))
                    })
            return data
        except Exception as e:
            logger.error(f"Greška pri dohvatanju događaja po tipu: {str(e)}")
            raise
    
    def get_dogadjaj_by_id(self, tip_dogadjaja: str, entitet_id: int) -> Optional[dict]:
        """
        Vraća jedan događaj po ID-u
        
        Args:
            tip_dogadjaja: "transakcija" ili "penal"
            entitet_id: ID faktore ili ugovora
        """
        flux_query = f'''
from(bucket: "{self.bucket}")
  |> range(start: -1y)
  |> filter(fn: (r) => r._measurement == "dogadjaji")
  |> filter(fn: (r) => r.tip_dogadjaja == "{tip_dogadjaja}")
  |> filter(fn: (r) => r.entitet_id == "{entitet_id}")
  |> filter(fn: (r) => r._field == "iznos" or r._field == "opis")
  |> pivot(rowKey: ["_time", "status"], columnKey: ["_field"], valueColumn: "_value")
  |> sort(columns: ["_time"], desc: true)
  |> limit(n: 1)
  |> yield(name: "dogadjaj")
        '''
        
        try:
            result = self.query_api.query(flux_query, org=self.org)
            for table in result:
                for record in table.records:
                    return {
                        "timestamp": record.get_time(),
                        "tip_dogadjaja": tip_dogadjaja,
                        "entitet_id": entitet_id,
                        "status": record.values.get('status'),
                        "iznos": float(record.values.get('iznos', 0)),
                        "opis": str(record.values.get('opis', ''))
                    }
            return None
        except Exception as e:
            logger.error(f"Greška pri dohvatanju događaja po ID: {str(e)}")
            raise
    
    def update_dogadjaj(
        self, 
        tip_dogadjaja: str, 
        entitet_id: int, 
        status: Optional[str] = None,
        iznos: Optional[float] = None,
        opis: Optional[str] = None
    ) -> bool:
        """
        GARANTOVANO AŽURIRANJE događaja - briše stari i kreira novi sa istim entitet_id
        
        Args:
            tip_dogadjaja: "transakcija" ili "penal"
            entitet_id: ID faktore ili ugovora
            status: Novi status (opciono)
            iznos: Novi iznos (opciono)
            opis: Novi opis (opciono)
        """
        import time
        from datetime import datetime, timedelta
        
        try:
            # Prvo dohvati postojeći događaj
            existing = self.get_dogadjaj_by_id(tip_dogadjaja, entitet_id)
            if not existing:
                logger.error(f"Događaj nije pronađen: {tip_dogadjaja} - {entitet_id}")
                return False
            
            # Pripremi nove vrednosti
            new_status = status if status is not None else existing['status']
            new_iznos = iznos if iznos is not None else existing['iznos']
            new_opis = opis if opis is not None else existing['opis']
            
            logger.info(f"🔄 Ažuriranje događaja: {tip_dogadjaja} - {entitet_id}")
            
            # GARANTOVANO BRISANJE starog zapisa - maksimalno širok opseg
            start_time = datetime(1970, 1, 1)
            stop_time = datetime.now() + timedelta(days=1)
            
            delete_api = self.client.delete_api()
            predicate = f'_measurement="dogadjaji" AND tip_dogadjaja="{tip_dogadjaja}" AND entitet_id="{entitet_id}"'
            
            delete_api.delete(
                start=start_time,
                stop=stop_time,
                predicate=predicate,
                bucket=self.bucket,
                org=self.org
            )
            
            # Čekaj da se brisanje propagira
            time.sleep(0.3)
            
            # Upiši novi zapis
            self.write_dogadjaj(
                tip_dogadjaja=tip_dogadjaja,
                status=new_status,
                entitet_id=entitet_id,
                iznos=new_iznos,
                opis=new_opis
            )
            
            # Verifikuj da je novi zapis upisan
            time.sleep(0.2)
            updated = self.get_dogadjaj_by_id(tip_dogadjaja, entitet_id)
            if updated is None:
                logger.error(f"❌ Novi zapis nije upisan!")
                raise Exception("Ažuriranje nije uspelo - novi zapis nije pronađen")
            
            logger.info(f"✅ POTVRĐENO: Događaj {tip_dogadjaja} - {entitet_id} je ažuriran")
            return True
        except Exception as e:
            logger.error(f"Greška pri ažuriranju događaja: {str(e)}")
            raise
    
    def delete_dogadjaj_by_id(self, tip_dogadjaja: str, entitet_id: int) -> bool:
        """
        GARANTOVANO TRENUTNO BRISANJE događaja po ID-u
        
        Briše SVE zapise sa datim tip_dogadjaja i entitet_id iz KOMPLETNE istorije.
        Koristi maksimalno širok vremenski opseg i verifikuje brisanje.
        
        Args:
            tip_dogadjaja: "transakcija" ili "penal"
            entitet_id: ID faktore ili ugovora
        """
        import time
        from datetime import datetime, timedelta
        
        try:
            # Prvo proveri da li događaj postoji
            existing = self.get_dogadjaj_by_id(tip_dogadjaja, entitet_id)
            if not existing:
                logger.warning(f"Događaj nije pronađen: {tip_dogadjaja} - {entitet_id}")
                return False
            
            logger.info(f"🗑️ Brisanje događaja: {tip_dogadjaja} - {entitet_id}")
            
            # GARANTOVANO BRISANJE - koristi maksimalno širok vremenski opseg
            # Briši SVE od početka vremena do 1 dan u budućnost
            start_time = datetime(1970, 1, 1)  # Unix epoch start
            stop_time = datetime.now() + timedelta(days=1)  # 1 dan u budućnost
            
            delete_api = self.client.delete_api()
            
            # PRVI POKUŠAJ - širok predicate sa entitet_id kao string
            predicate = f'_measurement="dogadjaji" AND tip_dogadjaja="{tip_dogadjaja}" AND entitet_id="{entitet_id}"'
            
            logger.info(f"Izvršavam DELETE sa predikatom: {predicate}")
            delete_api.delete(
                start=start_time,
                stop=stop_time,
                predicate=predicate,
                bucket=self.bucket,
                org=self.org
            )
            
            # ČEKAJ DA SE BRISANJE PROPAGIRA (InfluxDB je eventually consistent)
            time.sleep(0.3)  # 300ms čekanje
            
            # VERIFIKUJ BRISANJE - proveri da li još postoji
            max_retries = 3
            for attempt in range(max_retries):
                verify = self.get_dogadjaj_by_id(tip_dogadjaja, entitet_id)
                if verify is None:
                    logger.info(f"✅ POTVRĐENO: Događaj {tip_dogadjaja} - {entitet_id} je obrisan")
                    return True
                else:
                    logger.warning(f"⚠️ Pokušaj {attempt + 1}/{max_retries}: Događaj još postoji, čekam...")
                    time.sleep(0.2)  # Čekaj još 200ms
            
            # Ako nakon svih pokušaja još postoji - NUKLEARNO BRISANJE
            logger.error(f"🔥 NUKLEARNO BRISANJE: {tip_dogadjaja} - {entitet_id}")
            
            # Pokušaj sa različitim formatom predicate-a
            alternative_predicates = [
                f'_measurement="dogadjaji" AND tip_dogadjaja="{tip_dogadjaja}" AND entitet_id="{str(entitet_id)}"',
                f'_measurement="dogadjaji" AND tip_dogadjaja="{tip_dogadjaja}"',  # Brisanje svih istog tipa
            ]
            
            for alt_predicate in alternative_predicates:
                logger.info(f"Alternativni predicate: {alt_predicate}")
                delete_api.delete(
                    start=start_time,
                    stop=stop_time,
                    predicate=alt_predicate,
                    bucket=self.bucket,
                    org=self.org
                )
                time.sleep(0.3)
                
                verify = self.get_dogadjaj_by_id(tip_dogadjaja, entitet_id)
                if verify is None:
                    logger.info(f"✅ Događaj obrisan sa alternativnim predikatom")
                    return True
            
            # Ako ništa nije pomoglo
            logger.error(f"❌ KRITIČNO: Nije moguće obrisati događaj {tip_dogadjaja} - {entitet_id}")
            raise Exception(f"Brisanje nije uspelo nakon svih pokušaja")
            
        except Exception as e:
            logger.error(f"Greška pri brisanju događaja: {str(e)}")
            raise
    
    def query_dnevni_promet(self, days: int = 30) -> List[dict]:
        """
        Analizira dnevni promet - ukupna suma uspešnih transakcija po danima
        
        Flux upit: Izračunaj ukupnu sumu svih uspešnih uplata za svaki dan u poslednjih 30 dana
        Kombinuje filtriranje, grupisanje po danima, agregaciju (sum, count, mean) i sortiranje podataka
        """
        flux_query = f'''
from(bucket: "{self.bucket}")
  |> range(start: -{days}d)
  |> filter(fn: (r) => r._measurement == "dogadjaji")
  |> filter(fn: (r) => r.tip_dogadjaja == "transakcija")
  |> filter(fn: (r) => r.status == "uspesna")
  |> filter(fn: (r) => r._field == "iznos")
  |> aggregateWindow(every: 1d, fn: sum, createEmpty: false)
  |> group(columns: ["_time"])
  |> sum()
  |> sort(columns: ["_time"], desc: true)
  |> yield(name: "dnevni_promet")
        '''
        
        try:
            result = self.query_api.query(flux_query, org=self.org)
            data = []
            for table in result:
                for record in table.records:
                    data.append({
                        "datum": record.get_time().strftime("%Y-%m-%d"),
                        "ukupan_iznos": record.get_value()
                    })
            
            data.sort(key=lambda x: x["datum"], reverse=True)
            return data
        except Exception as e:
            logger.error(f"Greška pri upitu dnevnog prometa: {str(e)}")
            raise
    
    def query_rizicni_penali(self, min_iznos: float = 5000, limit: int = 10) -> List[dict]:
        """
        Identifikuje rizične penale

        Izvršava složeni Flux upit koji:
        - Filtrira penale po tipu i statusu
        - Kombinuje polja 'iznos' i 'opis' pivot transformacijom
        - Grupise podatke po ugovoru (entitet_id)
        - Agregira (sabira iznose i broji penale)
        - Sortira rezultate po ukupnom iznosu penala
        - Ograničava rezultat (limit)
        Sve se izvodi server-side u Flux-u (bez Python post-obrade).
        """
        flux_query = f'''
    from(bucket: "{self.bucket}")
    |> range(start: -1y)
    |> filter(fn: (r) => 
        r._measurement == "dogadjaji" and
        r.tip_dogadjaja == "penal" and
        r.status == "kreiran"
    )
    |> filter(fn: (r) => r._field == "iznos" or r._field == "opis")
    |> pivot(rowKey: ["_time", "entitet_id"], columnKey: ["_field"], valueColumn: "_value")
    |> filter(fn: (r) => float(v: r.iznos) > {min_iznos})
    |> group(columns: ["entitet_id"])
    |> reduce(
        fn: (r, accumulator) => ({{
            entitet_id: r.entitet_id,
            ukupan_iznos: accumulator.ukupan_iznos + float(v: r.iznos),
            broj_penala: accumulator.broj_penala + 1,
            poslednji_opis: if exists r.opis then r.opis else accumulator.poslednji_opis,
            poslednje_vreme: if exists r._time then r._time else accumulator.poslednje_vreme
        }}),
        identity: {{entitet_id: "", ukupan_iznos: 0.0, broj_penala: 0, poslednji_opis: "", poslednje_vreme: 2020-01-01T00:00:00Z}}
    )
    |> sort(columns: ["ukupan_iznos"], desc: true)
    |> limit(n: {limit})
    |> yield(name: "rizicni_penali")
        '''
        
        try:
            result = self.query_api.query(flux_query, org=self.org)
            data = []
            for table in result:
                for record in table.records:
                    # Osiguraj da je poslednje_vreme datetime objekat
                    poslednje_vreme = record.values.get("poslednje_vreme")
                    if isinstance(poslednje_vreme, str):
                        from datetime import datetime
                        poslednje_vreme = datetime.fromisoformat(poslednje_vreme.replace('Z', '+00:00'))
                    
                    data.append({
                        "entitet_id": int(record.values.get("entitet_id")),
                        "ukupan_iznos_po_ugovoru": float(record.values.get("ukupan_iznos", 0)),
                        "broj_penala_po_ugovoru": int(record.values.get("broj_penala", 0)),
                        "poslednji_opis": record.values.get("poslednji_opis", "N/A"),
                        "poslednje_vreme": poslednje_vreme
                    })
            return data
        except Exception as e:
            logger.error(f"Greška pri upitu rizičnih penala: {str(e)}")
            raise

    
    def query_uporedna_analiza(self, months: int = 3) -> List[dict]:
        """
        Uporedna analiza performansi
        
        Flux upit: Za svaku nedelju u poslednja 3 meseca, 
        izračunaj ukupan broj kreiranih penala i ukupan broj uspešnih transakcija
        """
        flux_query = f'''
import "date"

penali = from(bucket: "{self.bucket}")
  |> range(start: -{months}mo)
  |> filter(fn: (r) => r._measurement == "dogadjaji")
  |> filter(fn: (r) => r.tip_dogadjaja == "penal")
  |> filter(fn: (r) => r.status == "kreiran")
  |> filter(fn: (r) => r._field == "iznos")
  |> aggregateWindow(every: 1w, fn: count, createEmpty: false)
  |> set(key: "tip", value: "penal")

transakcije = from(bucket: "{self.bucket}")
  |> range(start: -{months}mo)
  |> filter(fn: (r) => r._measurement == "dogadjaji")
  |> filter(fn: (r) => r.tip_dogadjaja == "transakcija")
  |> filter(fn: (r) => r.status == "uspesna")
  |> filter(fn: (r) => r._field == "iznos")
  |> aggregateWindow(every: 1w, fn: count, createEmpty: false)
  |> set(key: "tip", value: "transakcija")

union(tables: [penali, transakcije])
  |> yield(name: "uporedna_analiza")
        '''
        
        try:
            result = self.query_api.query(flux_query, org=self.org)
            
            # Grupisanje po nedeljama
            weekly_data = {}
            for table in result:
                for record in table.records:
                    week_key = record.get_time().strftime("%Y-W%U")
                    if week_key not in weekly_data:
                        weekly_data[week_key] = {
                            "nedelja": week_key,
                            "broj_penala": 0,
                            "broj_transakcija": 0
                        }
                    
                    tip = record.values.get("tip")
                    count = record.get_value()
                    
                    if tip == "penal":
                        weekly_data[week_key]["broj_penala"] += count
                    elif tip == "transakcija":
                        weekly_data[week_key]["broj_transakcija"] += count
            
            # Sortiraj rezultate po nedelji (najnovije prvo)
            sorted_data = sorted(weekly_data.values(), key=lambda x: x["nedelja"], reverse=True)
            return sorted_data
        except Exception as e:
            logger.error(f"Greška pri uporednoj analizi: {str(e)}")
            raise
    
    def close(self):
        """Zatvara konekciju sa InfluxDB"""
        self.client.close()


influx_service = InfluxDBService()