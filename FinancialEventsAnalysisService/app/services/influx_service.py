from influxdb_client import InfluxDBClient, Point
from influxdb_client.client.write_api import SYNCHRONOUS
from datetime import datetime, timedelta
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
    
    def query_dnevni_promet(self, days: int = 30) -> List[dict]:
        """
        Analizira dnevni promet - ukupna suma uspešnih transakcija po danima
        
        Flux upit: Izračunaj ukupnu suму svих uspešnih uplata za svaki dan u poslednjih 30 dana
        """
        flux_query = f'''
from(bucket: "{self.bucket}")
  |> range(start: -{days}d)
  |> filter(fn: (r) => r._measurement == "dogadjaji")
  |> filter(fn: (r) => r.tip_dogadjaja == "transakcija")
  |> filter(fn: (r) => r.status == "uspesna")
  |> filter(fn: (r) => r._field == "iznos")
  |> aggregateWindow(every: 1d, fn: sum, createEmpty: false)
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
        # Prvi upit: Dohvati sve penale veće od min_iznos sa opsom
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
        
        # Drugi upit: Agregacija - ukupan iznos i broj penala po ugovoru
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
            # Dohvati podatke sa opisom
            result_data = self.query_api.query(flux_query_data, org=self.org)
            
            # Prikupi podatke i uradi Python agregaciju
            # (jer Flux agregacija sa sum i count u jednom upitu je kompleksna)
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
            
            # Agregacija u Python-u - grupisanje po entitet_id
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
            
            return list(weekly_data.values())
        except Exception as e:
            logger.error(f"Greška pri uporednoj analizi: {str(e)}")
            raise
    
    def close(self):
        """Zatvara konekciju sa InfluxDB"""
        self.client.close()


# Singleton instanca
influx_service = InfluxDBService()