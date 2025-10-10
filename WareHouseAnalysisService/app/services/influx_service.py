from influxdb_client import InfluxDBClient, Point
from influxdb_client.client.write_api import SYNCHRONOUS
from datetime import datetime, timedelta
import logging
from app.config.settings import settings
from typing import List, Optional

logger = logging.getLogger(__name__)


class InfluxDBService:
    """Servis za rad sa InfluxDB bazom podataka za skladišne uslove"""
    
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
    
    def _determine_temperature_status(self, temperatura: float) -> str:
        """Određuje status temperature"""
        if (temperatura < settings.KRITICNA_TEMPERATURA_MIN or 
            temperatura > settings.KRITICNA_TEMPERATURA_MAX):
            return "kritična"
        elif (temperatura < settings.OPTIMALNA_TEMPERATURA_MIN or 
              temperatura > settings.OPTIMALNA_TEMPERATURA_MAX):
            return "rizična"
        else:
            return "optimalna"
    
    def _determine_humidity_status(self, vlaznost: float) -> str:
        """Određuje status vlažnosti"""
        if (vlaznost < settings.KRITICNA_VLAZNOST_MIN or 
            vlaznost > settings.KRITICNA_VLAZNOST_MAX):
            return "kritična"
        elif (vlaznost < settings.OPTIMALNA_VLAZNOST_MIN or 
              vlaznost > settings.OPTIMALNA_VLAZNOST_MAX):
            return "rizična"
        else:
            return "optimalna"

    def write_merenje_temperature(self, skladiste_id: int, temperatura: float, senzor_id: str, lokacija: str, timestamp: Optional[datetime] = None):
        """CREATE - Upisuje merenje temperature"""
        try:
            status = self._determine_temperature_status(temperatura)
            point = Point("merenja_temperatura") \
                .tag("skladiste_id", str(skladiste_id)) \
                .tag("senzor_id", senzor_id) \
                .tag("lokacija", lokacija) \
                .tag("status", status) \
                .field("vrednost", float(temperatura)) \
                .field("tip_merenja", "temperatura")
            
            if timestamp:
                point = point.time(timestamp)
            
            self.write_api.write(bucket=self.bucket, org=self.org, record=point)
            logger.info(f"Uspešno upisano merenje temperature: Skladište {skladiste_id}, {temperatura}°C")
            return True
        except Exception as e:
            logger.error(f"Greška pri upisu merenja temperature: {str(e)}")
            raise

    def write_merenje_vlaznost(self, skladiste_id: int, vlaznost: float, senzor_id: str, lokacija: str, timestamp: Optional[datetime] = None):
        """CREATE - Upisuje merenje vlažnosti"""
        try:
            status = self._determine_humidity_status(vlaznost)
            point = Point("merenja_vlaznost") \
                .tag("skladiste_id", str(skladiste_id)) \
                .tag("senzor_id", senzor_id) \
                .tag("lokacija", lokacija) \
                .tag("status", status) \
                .field("vrednost", float(vlaznost)) \
                .field("tip_merenja", "vlaznost")
            
            if timestamp:
                point = point.time(timestamp)
            
            self.write_api.write(bucket=self.bucket, org=self.org, record=point)
            logger.info(f"Uspešno upisano merenje vlažnosti: Skladište {skladiste_id}, {vlaznost}%")
            return True
        except Exception as e:
            logger.error(f"Greška pri upisu merenja vlažnosti: {str(e)}")
            raise

    def delete_merenje_temperature(self, skladiste_id: int, senzor_id: str, timestamp: datetime) -> bool:
        """DELETE - Briše pojedinačno merenje temperature"""
        try:
            delete_api = self.client.delete_api()
            predicate = f'_measurement="merenja_temperatura" AND skladiste_id="{skladiste_id}" AND senzor_id="{senzor_id}"'
            
            delete_api.delete(
                start=timestamp,
                stop=timestamp + timedelta(seconds=1),
                predicate=predicate,
                bucket=self.bucket,
                org=self.org
            )
            logger.info(f"Obrisano merenje temperature: {skladiste_id}/{senzor_id}")
            return True
        except Exception as e:
            logger.error(f"Greška pri brisanju merenja temperature: {str(e)}")
            raise

    def delete_merenje_vlaznost(self, skladiste_id: int, senzor_id: str, timestamp: datetime) -> bool:
        """DELETE - Briše pojedinačno merenje vlažnosti"""
        try:
            delete_api = self.client.delete_api()
            predicate = f'_measurement="merenja_vlaznost" AND skladiste_id="{skladiste_id}" AND senzor_id="{senzor_id}"'
            
            delete_api.delete(
                start=timestamp,
                stop=timestamp + timedelta(seconds=1),
                predicate=predicate,
                bucket=self.bucket,
                org=self.org
            )
            logger.info(f"Obrisano merenje vlažnosti: {skladiste_id}/{senzor_id}")
            return True
        except Exception as e:
            logger.error(f"Greška pri brisanju merenja vlažnosti: {str(e)}")
            raise

    def query_complex_1_daily_stats_by_warehouse(self, days: int = 30) -> List[dict]:
        """SLOŽEN UPIT 1: Dnevne statistike po skladištu"""
        flux_query = f'''
from(bucket: "{self.bucket}")
  |> range(start: -{days}d)
  |> filter(fn: (r) => r._measurement == "merenja_temperatura" or r._measurement == "merenja_vlaznost")
  |> filter(fn: (r) => r._field == "vrednost")
  |> group(columns: ["_measurement", "skladiste_id"])
  |> aggregateWindow(every: 1d, fn: mean, createEmpty: false)
  |> group(columns: ["_time", "skladiste_id"])
  |> pivot(rowKey: ["_time", "skladiste_id"], columnKey: ["_measurement"], valueColumn: "_value")
  |> sort(columns: ["_time"], desc: true)
        '''
        
        try:
            result = self.query_api.query(flux_query, org=self.org)
            data = []
            for table in result:
                for record in table.records:
                    data.append({
                        "datum": record.get_time().strftime("%Y-%m-%d"),
                        "skladiste_id": int(record.values.get('skladiste_id')),
                        "prosecna_temperatura": round(float(record.values.get('merenja_temperatura', 0)), 2),
                        "prosecna_vlaznost": round(float(record.values.get('merenja_vlaznost', 0)), 2),
                    })
            return data
        except Exception as e:
            logger.error(f"Greška pri složenom upitu 1: {str(e)}")
            raise

    def query_complex_2_critical_conditions_aggregated(self, days: int = 365) -> List[dict]:
        """SLOŽEN UPIT 2: Agregacija kritičnih i rizičnih uslova"""
        flux_query = f'''
temp_critical = from(bucket: "{self.bucket}")
  |> range(start: -{days}d)
  |> filter(fn: (r) => r._measurement == "merenja_temperatura")
  |> filter(fn: (r) => r._field == "vrednost")
  |> filter(fn: (r) => r.status == "kritična" or r.status == "rizična")
  |> group(columns: ["skladiste_id"])
  |> count(column: "_value")
  |> set(key: "tip_merenja", value: "temperatura")

vlaz_critical = from(bucket: "{self.bucket}")
  |> range(start: -{days}d)
  |> filter(fn: (r) => r._measurement == "merenja_vlaznost")
  |> filter(fn: (r) => r._field == "vrednost")
  |> filter(fn: (r) => r.status == "kritična" or r.status == "rizična")
  |> group(columns: ["skladiste_id"])
  |> count(column: "_value")
  |> set(key: "tip_merenja", value: "vlaznost")

union(tables: [temp_critical, vlaz_critical])
  |> group(columns: ["skladiste_id"])
  |> sort(columns: ["_value"], desc: true)
        '''
        
        try:
            result = self.query_api.query(flux_query, org=self.org)
            warehouse_stats = {}
            for table in result:
                for record in table.records:
                    skladiste_id = int(record.values.get('skladiste_id'))
                    tip_merenja = record.values.get('tip_merenja')
                    broj_kriticnih = record.get_value()
                    
                    if skladiste_id not in warehouse_stats:
                        warehouse_stats[skladiste_id] = {
                            "skladiste_id": skladiste_id,
                            "problematicni_temperatura": 0,
                            "problematicni_vlaznost": 0,
                            "ukupno_problematicnih": 0
                        }
                    
                    if tip_merenja == "temperatura":
                        warehouse_stats[skladiste_id]["problematicni_temperatura"] = broj_kriticnih
                    elif tip_merenja == "vlaznost":
                        warehouse_stats[skladiste_id]["problematicni_vlaznost"] = broj_kriticnih
                    
                    warehouse_stats[skladiste_id]["ukupno_problematicnih"] = (
                        warehouse_stats[skladiste_id]["problematicni_temperatura"] +
                        warehouse_stats[skladiste_id]["problematicni_vlaznost"]
                    )
            
            sorted_data = sorted(warehouse_stats.values(), 
                               key=lambda x: x["ukupno_problematicnih"], 
                               reverse=True)
            
            return sorted_data
        except Exception as e:
            logger.error(f"Greška pri složenom upitu 2: {str(e)}")
            raise

    def query_complex_3_sensor_performance_ranking(self, days: int = 14) -> List[dict]:
        """SLOŽEN UPIT 3: Ranking performansi senzora"""
        flux_query = f'''
from(bucket: "{self.bucket}")
  |> range(start: -{days}d)
  |> filter(fn: (r) => r._measurement == "merenja_temperatura" or r._measurement == "merenja_vlaznost")
  |> filter(fn: (r) => r._field == "vrednost")
  |> group(columns: ["skladiste_id", "senzor_id", "lokacija", "_measurement"])
  |> aggregateWindow(every: 1h, fn: mean, createEmpty: false)
  |> group(columns: ["skladiste_id", "senzor_id", "lokacija"])
  |> count(column: "_value")
  |> sort(columns: ["_value"], desc: true)
        '''
        
        try:
            result = self.query_api.query(flux_query, org=self.org)
            data = []
            for table in result:
                for record in table.records:
                    data.append({
                        "skladiste_id": int(record.values.get('skladiste_id')),
                        "senzor_id": record.values.get('senzor_id'),
                        "lokacija": record.values.get('lokacija'),
                        "broj_merenja": record.get_value(),
                        "rang": len(data) + 1
                    })
            return data
        except Exception as e:
            logger.error(f"Greška pri složenom upitu 3: {str(e)}")
            raise

    def close(self):
        """Zatvara konekciju sa InfluxDB"""
        self.client.close()


influx_service = InfluxDBService()