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
        Ažurira događaj - tehnički briše stari i kreira novi sa istim entitet_id
        
        Args:
            tip_dogadjaja: "transakcija" ili "penal"
            entitet_id: ID faktore ili ugovora
            status: Novi status (opciono)
            iznos: Novi iznos (opciono)
            opis: Novi opis (opciono)
        """
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
            
            # Obriši stari zapis - InfluxDB ne podržava update, već delete + insert
            timestamp = existing['timestamp']
            delete_api = self.client.delete_api()
            delete_api.delete(
                start=timestamp,
                stop=timestamp,
                predicate=f'_measurement="dogadjaji" AND tip_dogadjaja="{tip_dogadjaja}" AND entitet_id="{entitet_id}"',
                bucket=self.bucket,
                org=self.org
            )
            
            # Upiši novi zapis
            self.write_dogadjaj(
                tip_dogadjaja=tip_dogadjaja,
                status=new_status,
                entitet_id=entitet_id,
                iznos=new_iznos,
                opis=new_opis
            )
            
            logger.info(f"Uspešno ažuriran događaj: {tip_dogadjaja} - {entitet_id}")
            return True
        except Exception as e:
            logger.error(f"Greška pri ažuriranju događaja: {str(e)}")
            raise
    
    def delete_dogadjaj_by_id(self, tip_dogadjaja: str, entitet_id: int) -> bool:
        """
        Briše događaj po ID-u
        
        Args:
            tip_dogadjaja: "transakcija" ili "penal"
            entitet_id: ID faktore ili ugovora
        """
        try:
            # Prvo proveri da li događaj postoji
            existing = self.get_dogadjaj_by_id(tip_dogadjaja, entitet_id)
            if not existing:
                logger.warning(f"Događaj nije pronađen: {tip_dogadjaja} - {entitet_id}")
                return False
            
            # Obriši događaj
            timestamp = existing['timestamp']
            delete_api = self.client.delete_api()
            delete_api.delete(
                start=timestamp,
                stop=timestamp,
                predicate=f'_measurement="dogadjaji" AND tip_dogadjaja="{tip_dogadjaja}" AND entitet_id="{entitet_id}"',
                bucket=self.bucket,
                org=self.org
            )
            
            logger.info(f"Uspešno obrisan događaj: {tip_dogadjaja} - {entitet_id}")
            return True
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
  |> group(columns: ["_time"], mode: "by")
  |> aggregateWindow(every: 1d, fn: sum, createEmpty: false)
  |> group()
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
        
        Flux upit: Prikaži ugovore sa najrizičnijim penalima (iznos > 5000 RSD)
        Kombinuje filtriranje, pivot transformaciju, grupisanje, agregaciju i sortiranje podataka
        Za svaki ugovor agregira ukupan iznos penala i broj penala
        """
        flux_query_data = f'''
from(bucket: "{self.bucket}")
  |> range(start: -1y)
  |> filter(fn: (r) => r._measurement == "dogadjaji")
  |> filter(fn: (r) => r.tip_dogadjaja == "penal")
  |> filter(fn: (r) => r.status == "kreiran")
  |> filter(fn: (r) => r._field == "iznos" or r._field == "opis")
  |> pivot(rowKey: ["_time", "entitet_id"], columnKey: ["_field"], valueColumn: "_value")
  |> filter(fn: (r) => float(v: r.iznos) > {min_iznos})
  |> sort(columns: ["_time"], desc: true)
  |> yield(name: "penali_sa_opisom")
        '''
        
        flux_query_agg = f'''
from(bucket: "{self.bucket}")
  |> range(start: -1y)
  |> filter(fn: (r) => r._measurement == "dogadjaji")
  |> filter(fn: (r) => r.tip_dogadjaja == "penal")
  |> filter(fn: (r) => r.status == "kreiran")
  |> filter(fn: (r) => r._field == "iznos")
  |> filter(fn: (r) => r._value > {min_iznos})
  |> group(columns: ["entitet_id"])
  |> sum(column: "_value")
  |> set(key: "ukupan_iznos", value: string(v: int(v: r._value)))
  |> count(column: "_value")
  |> set(key: "broj_penala", value: string(v: int(v: r._value)))
  |> group()
  |> yield(name: "agregacija_po_ugovoru")
        '''
        
        try:
            result_data = self.query_api.query(flux_query_data, org=self.org)
            
            penali_data = []
            for table in result_data:
                for record in table.records:
                    timestamp = record.get_time()
                    entitet_id = record.values.get('entitet_id')
                    iznos = record.values.get('iznos')
                    opis = record.values.get('opis', 'N/A')
                    
                    penali_data.append({
                        "timestamp": timestamp,
                        "entitet_id": entitet_id,
                        "iznos": float(iznos) if iznos is not None else 0.0,
                        "opis": str(opis) if opis else "N/A"
                    })
            
            # Agregacija po ugovoru
            ugovor_stats = {}
            for penal in penali_data:
                entitet_id = penal['entitet_id']
                if entitet_id not in ugovor_stats:
                    ugovor_stats[entitet_id] = {
                        'ukupan_iznos': 0.0,
                        'broj_penala': 0
                    }
                ugovor_stats[entitet_id]['ukupan_iznos'] += penal['iznos']
                ugovor_stats[entitet_id]['broj_penala'] += 1
            
            # Sortiraj po vremenu i primeni limit
            penali_data.sort(key=lambda x: x['timestamp'], reverse=True)
            limited_data = penali_data[:limit]
            
            # Dodaj agregirane statistike svakom penalu
            data = []
            for penal in limited_data:
                entitet_id = penal['entitet_id']
                stats = ugovor_stats[entitet_id]
                
                data.append({
                    "timestamp": penal['timestamp'],
                    "entitet_id": int(entitet_id),
                    "iznos": penal['iznos'],
                    "opis": penal['opis'],
                    "ukupan_iznos_po_ugovoru": float(stats['ukupan_iznos']),
                    "broj_penala_po_ugovoru": int(stats['broj_penala'])
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