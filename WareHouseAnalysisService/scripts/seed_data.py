"""
Skripta za popunjavanje InfluxDB sa test podacima o skladišnim uslovima

Kreira realističke podatke o temperaturi i vlažnosti za 3 skladišta:
- Skladište 1: Hladnjača (2-8°C, 80-95% vlažnost)
- Skladište 2: Standardno (18-24°C, 40-60% vlažnost) 
- Skladište 3: Suvo skladište (20-30°C, 20-40% vlažnost)

Generiše merenja za poslednja 30 dana sa različitim senzorima i lokacijama.
"""

import random
import math
from datetime import datetime, timedelta
from influxdb_client import InfluxDBClient, Point
from influxdb_client.client.write_api import SYNCHRONOUS
import os
from dotenv import load_dotenv

# Učitaj env varijable
load_dotenv()

# InfluxDB konfiguracija
INFLUXDB_URL = os.getenv("INFLUXDB_URL", "http://localhost:8087")
INFLUXDB_TOKEN = os.getenv("INFLUXDB_TOKEN", "warehouse-super-secret-auth-token")
INFLUXDB_ORG = os.getenv("INFLUXDB_ORG", "IIS_SUDPI")
INFLUXDB_BUCKET = os.getenv("INFLUXDB_BUCKET", "skladisni_uslovi")

# Konstante - ukupno 1000 merenja po tipu (200 po skladištu)
BROJ_MERENJA_TEMPERATURA = 200   # 200 merenja temperature po skladištu (ukupno 1000) 
BROJ_MERENJA_VLAZNOST = 200      # 200 merenja vlažnosti po skladištu (ukupno 1000)
BROJ_DANA = 60                   # Period za generisanje podataka
DATUM_START = datetime.now() - timedelta(days=BROJ_DANA)
DATUM_END = datetime.now()

# Definisanje 5 opštih skladišta (bez komplikovanja tipova)
SKLADISTA = {
    1: {
        "naziv": "Skladište 1",
        "tip_robe": "Opšta roba",
        "temp_min": 18.0,
        "temp_max": 24.0,
        "temp_optimalna": 21.0,
        "vlaz_min": 40.0,
        "vlaz_max": 60.0,
        "vlaz_optimalna": 50.0,
        "senzori": ["TEMP_S1_01", "TEMP_S1_02", "HUMID_S1_01", "HUMID_S1_02"],
        "lokacije": ["Zona A", "Zona B", "Zona C", "Rampa"]
    },
    2: {
        "naziv": "Skladište 2", 
        "tip_robe": "Opšta roba",
        "temp_min": 18.0,
        "temp_max": 24.0,
        "temp_optimalna": 21.0,
        "vlaz_min": 40.0,
        "vlaz_max": 60.0,
        "vlaz_optimalna": 50.0,
        "senzori": ["TEMP_S2_01", "TEMP_S2_02", "HUMID_S2_01", "HUMID_S2_02"],
        "lokacije": ["Sektor 1", "Sektor 2", "Sektor 3", "Ulaz"]
    },
    3: {
        "naziv": "Skladište 3",
        "tip_robe": "Opšta roba", 
        "temp_min": 18.0,
        "temp_max": 24.0,
        "temp_optimalna": 21.0,
        "vlaz_min": 40.0,
        "vlaz_max": 60.0,
        "vlaz_optimalna": 50.0,
        "senzori": ["TEMP_S3_01", "TEMP_S3_02", "HUMID_S3_01", "HUMID_S3_02"],
        "lokacije": ["Polica A", "Polica B", "Polica C", "Centrala"]
    },
    4: {
        "naziv": "Skladište 4",
        "tip_robe": "Opšta roba",
        "temp_min": 18.0,
        "temp_max": 24.0,
        "temp_optimalna": 21.0,
        "vlaz_min": 40.0,
        "vlaz_max": 60.0,
        "vlaz_optimalna": 50.0,
        "senzori": ["TEMP_S4_01", "TEMP_S4_02", "HUMID_S4_01", "HUMID_S4_02"],
        "lokacije": ["Deo A", "Deo B", "Deo C", "Kontrola"]
    },
    5: {
        "naziv": "Skladište 5",
        "tip_robe": "Opšta roba",
        "temp_min": 18.0,
        "temp_max": 24.0,
        "temp_optimalna": 21.0,
        "vlaz_min": 40.0,
        "vlaz_max": 60.0,
        "vlaz_optimalna": 50.0,
        "senzori": ["TEMP_S5_01", "TEMP_S5_02", "HUMID_S5_01", "HUMID_S5_02"],
        "lokacije": ["Oblast 1", "Oblast 2", "Oblast 3", "Ispitivanje"]
    }
}


# Ukloniti stare funkcije koje nisu potrebne za nove zahteve


def generate_merenja_temperature(write_api, skladiste_id: int, info: dict, count: int):
    """Generiše ODVOJENA merenja SAMO za temperaturu"""
    print(f"Generisanje {count} merenja TEMPERATURE za Skladište {skladiste_id}")
    
    current_temp = info["temp_optimalna"] + random.uniform(-1, 1)
    
    for i in range(count):
        # Nasumični timestamp u periodu
        timestamp = random_datetime_in_period(DATUM_START, DATUM_END)
        
        # Generisanje temperature sa realističnim varijacijama
        temp_change = random.uniform(-0.5, 0.5)
        current_temp += temp_change
        
        # Vraćanje ka optimalnoj vrednosti
        temp_drift = (info["temp_optimalna"] - current_temp) * 0.02
        current_temp += temp_drift
        
        # Finalna temperatura
        temperatura = max(info["temp_min"] - 3, 
                         min(info["temp_max"] + 3, current_temp))
        
        # Određuj status temperature
        if temperatura < 18 or temperatura > 24:
            if temperatura < 10 or temperatura > 35:
                status_temp = "kritična"
            else:
                status_temp = "rizična"
        else:
            status_temp = "optimalna"
        
        # Nasumično odaberi senzor temperature i lokaciju
        temp_senzori = [s for s in info["senzori"] if "TEMP" in s]
        senzor_id = random.choice(temp_senzori if temp_senzori else info["senzori"])
        lokacija = random.choice(info["lokacije"])
        
        # Kreiranje Point objekta SAMO za temperaturu
        point = Point("merenja_temperatura") \
            .tag("skladiste_id", str(skladiste_id)) \
            .tag("senzor_id", senzor_id) \
            .tag("lokacija", lokacija) \
            .tag("status", status_temp) \
            .field("vrednost", round(temperatura, 2)) \
            .field("tip_merenja", "temperatura") \
            .time(timestamp)
        
        write_api.write(bucket=INFLUXDB_BUCKET, org=INFLUXDB_ORG, record=point)
        
        if (i + 1) % 200 == 0:
            print(f"  Temperature: {i + 1}/{count}")
    
    print(f"  Uspešno kreirano {count} merenja temperature za skladište {skladiste_id}")


def generate_merenja_vlaznost(write_api, skladiste_id: int, info: dict, count: int):
    """Generiše ODVOJENA merenja SAMO za vlažnost"""
    print(f"Generisanje {count} merenja VLAŽNOSTI za Skladište {skladiste_id}")
    
    current_humid = info["vlaz_optimalna"] + random.uniform(-3, 3)
    
    for i in range(count):
        # Nasumični timestamp u periodu
        timestamp = random_datetime_in_period(DATUM_START, DATUM_END)
        
        # Generisanje vlažnosti sa realističnim varijacijama
        humid_change = random.uniform(-1.5, 1.5)
        current_humid += humid_change
        
        # Vraćanje ka optimalnoj vrednosti
        humid_drift = (info["vlaz_optimalna"] - current_humid) * 0.03
        current_humid += humid_drift
        
        # Finalna vlažnost
        vlaznost = max(info["vlaz_min"] - 10, 
                      min(info["vlaz_max"] + 10, current_humid))
        
        # Određuj status vlažnosti
        if vlaznost < 40 or vlaznost > 60:
            if vlaznost < 20 or vlaznost > 80:
                status_vlaz = "kritična"
            else:
                status_vlaz = "rizična"
        else:
            status_vlaz = "optimalna"
        
        # Nasumično odaberi senzor vlažnosti i lokaciju
        humid_senzori = [s for s in info["senzori"] if "HUMID" in s]
        senzor_id = random.choice(humid_senzori if humid_senzori else info["senzori"])
        lokacija = random.choice(info["lokacije"])
        
        # Kreiranje Point objekta SAMO za vlažnost
        point = Point("merenja_vlaznost") \
            .tag("skladiste_id", str(skladiste_id)) \
            .tag("senzor_id", senzor_id) \
            .tag("lokacija", lokacija) \
            .tag("status", status_vlaz) \
            .field("vrednost", round(vlaznost, 2)) \
            .field("tip_merenja", "vlaznost") \
            .time(timestamp)
        
        write_api.write(bucket=INFLUXDB_BUCKET, org=INFLUXDB_ORG, record=point)
        
        if (i + 1) % 200 == 0:
            print(f"  Vlažnost: {i + 1}/{count}")
    
    print(f"  Uspešno kreirano {count} merenja vlažnosti za skladište {skladiste_id}")


def random_datetime_in_period(start: datetime, end: datetime) -> datetime:
    """Generiše nasumični datum u periodu"""
    delta = end - start
    random_days = random.randint(0, delta.days)
    random_seconds = random.randint(0, 86399)
    return start + timedelta(days=random_days, seconds=random_seconds)


def main():
    """Glavna funkcija"""
    print("SEED SKRIPTA ZA INFLUXDB - SKLADIŠNI USLOVI")
    print("=" * 50)

    print(f"URL: {INFLUXDB_URL}")
    print(f"Organizacija: {INFLUXDB_ORG}")
    print(f"Bucket: {INFLUXDB_BUCKET}")
    print(f"Period: {DATUM_START.strftime('%Y-%m-%d')} - {DATUM_END.strftime('%Y-%m-%d')}")
    print(f"Broj dana: {BROJ_DANA}")
    print(f"Merenja temperature po skladištu: {BROJ_MERENJA_TEMPERATURA}")
    print(f"Merenja vlažnosti po skladištu: {BROJ_MERENJA_VLAZNOST}")
    
    try:
        # Konekcija sa InfluxDB
        print("\\nUspostavljanje konekcije sa InfluxDB...")
        client = InfluxDBClient(
            url=INFLUXDB_URL,
            token=INFLUXDB_TOKEN,
            org=INFLUXDB_ORG
        )
        
        # Write API
        write_api = client.write_api(write_options=SYNCHRONOUS)
        
        print("Konekcija uspešno uspostavljena\\n")
        
        # Informacije o skladištima
        print("SKLADIŠTA (sva opšta):")
        for sid, info in SKLADISTA.items():
            print(f"  {sid}. {info['naziv']} - {info['tip_robe']}")
            print(f"     Temperatura: {info['temp_min']}-{info['temp_max']}°C")
            print(f"     Vlažnost: {info['vlaz_min']}-{info['vlaz_max']}%")
        print()
        
        # Generisanje ODVOJENIH merenja za svako skladište
        ukupno_temp_merenja = 0
        ukupno_vlaz_merenja = 0
        
        for skladiste_id, info in SKLADISTA.items():
            print(f"\\n--- SKLADIŠTE {skladiste_id} ---")
            
            # Generiši merenja temperature
            generate_merenja_temperature(write_api, skladiste_id, info, BROJ_MERENJA_TEMPERATURA)
            ukupno_temp_merenja += BROJ_MERENJA_TEMPERATURA
            
            # Generiši merenja vlažnosti
            generate_merenja_vlaznost(write_api, skladiste_id, info, BROJ_MERENJA_VLAZNOST)
            ukupno_vlaz_merenja += BROJ_MERENJA_VLAZNOST
        
        # Zatvaranje konekcije
        client.close()
        
        print("\\n" + "=" * 60)
        print("USPEŠNO ZAVRŠENO!")
        print(f"SKLADIŠTA: {len(SKLADISTA)} (ID 1-5)")
        print(f"MERENJA TEMPERATURE: {ukupno_temp_merenja} (bucket: merenja_temperatura)")
        print(f"MERENJA VLAŽNOSTI: {ukupno_vlaz_merenja} (bucket: merenja_vlaznost)")
        print(f"UKUPNO MERENJA: {ukupno_temp_merenja + ukupno_vlaz_merenja}")
        print(f"Period: {BROJ_DANA} dana")
        
        print("\\nZAHTEVI ISPUNJENI:")
        print(f"✅ Baza sadrži 1 bucket '{INFLUXDB_BUCKET}'")
        print(f"✅ 2 logičke jedinice: 'merenja_temperatura' i 'merenja_vlaznost'")
        print(f"✅ {BROJ_MERENJA_TEMPERATURA} slogova po merenju temperature")
        print(f"✅ {BROJ_MERENJA_VLAZNOST} slogova po merenju vlažnosti") 
        print(f"✅ Skladišta ID: 1, 2, 3, 4, 5")
        print(f"✅ Svi tipovi opšti (bez komplikovanja)")
        
    except Exception as e:
        print(f"\\nGREŠKA: {str(e)}")
        raise


if __name__ == "__main__":
    main()