"""
Skripta za popunjavanje InfluxDB sa test podacima

Kreira 2000 događaja:
- 1500 transakcija (sa različitim statusima)
- 500 penala (sa različitim statusima)

Događaji se kreiraju za poslednja 3 meseca sa realističnim raspodelom.
"""

import random
from datetime import datetime, timedelta
from influxdb_client import InfluxDBClient, Point
from influxdb_client.client.write_api import SYNCHRONOUS
import os
from dotenv import load_dotenv

# Učitaj env varijable
load_dotenv()

# InfluxDB konfiguracija
INFLUXDB_URL = os.getenv("INFLUXDB_URL", "http://localhost:8086")
INFLUXDB_TOKEN = os.getenv("INFLUXDB_TOKEN", "my-super-secret-auth-token")
INFLUXDB_ORG = os.getenv("INFLUXDB_ORG", "IIS_SUDPI")
INFLUXDB_BUCKET = os.getenv("INFLUXDB_BUCKET", "finansijski_dogadjaji")

# Konstante
BROJ_TRANSAKCIJA = 1500
BROJ_PENALA = 500
DATUM_START = datetime.now() - timedelta(days=90)  # Pre 3 meseca
DATUM_END = datetime.now()

# Statusni transakcija i njihove verovatnoće
TRANSAKCIJA_STATUSI = {
    "uspesna": 0.75,      # 75% uspešnih
    "neuspesna": 0.15,    # 15% neuspešnih
    "na_cekanju": 0.10    # 10% na čekanju
}

# Statusni penala
PENAL_STATUSI = {
    "kreiran": 0.70,      # 70% kreiranih
    "placen": 0.30        # 30% plaćenih
}

# Opsezi iznosa
TRANSAKCIJA_IZNOS_MIN = 1000
TRANSAKCIJA_IZNOS_MAX = 500000

PENAL_IZNOS_MIN = 500
PENAL_IZNOS_MAX = 50000

# Opisi
TRANSAKCIJA_OPISI = [
    "Plaćanje fakture za isporučenu robu",
    "Uplata po ugovoru o nabavci",
    "Redovno mesečno plaćanje",
    "Avansna uplata",
    "Konačna uplata po ugovoru",
    "Plaćanje za pružene usluge",
    "Kompenzacija duga",
    "Delimična uplata",
]

PENAL_RAZLOZI = [
    "Kašnjenje u isporuci robe",
    "Neispunjavanje ugovornih obaveza",
    "Nedostatak kvaliteta isporučene robe",
    "Propuštanje roka isporuke",
    "Nepoštovanje standardа kvaliteta",
    "Kašnjenje sa dokumentacijom",
    "Nepotpuna isporuka",
    "Oštećena roba pri isporuci",
    "Nekompletna dokumentacija",
    "Neispravna roba",
]


def random_datetime(start: datetime, end: datetime) -> datetime:
    """Generise nasumični datum između start i end"""
    delta = end - start
    random_days = random.randint(0, delta.days)
    random_seconds = random.randint(0, 86400)
    return start + timedelta(days=random_days, seconds=random_seconds)


def weighted_choice(choices: dict) -> str:
    """Vrši izbor na osnovu težina/verovatnoća"""
    population = list(choices.keys())
    weights = list(choices.values())
    return random.choices(population, weights=weights)[0]


def generate_transakcije(write_api, count: int):
    """Generiše transakcije"""
    print(f"Generisanje {count} transakcija...")
    
    for i in range(count):
        timestamp = random_datetime(DATUM_START, DATUM_END)
        status = weighted_choice(TRANSAKCIJA_STATUSI)
        faktura_id = random.randint(1, 1000)
        iznos = round(random.uniform(TRANSAKCIJA_IZNOS_MIN, TRANSAKCIJA_IZNOS_MAX), 2)
        opis = random.choice(TRANSAKCIJA_OPISI)
        
        # Kreiranje Point objekta
        point = Point("dogadjaji") \
            .tag("tip_dogadjaja", "transakcija") \
            .tag("status", status) \
            .tag("entitet_id", str(faktura_id)) \
            .field("iznos", iznos) \
            .field("opis", opis) \
            .time(timestamp)
        
        # Upis u InfluxDB
        write_api.write(bucket=INFLUXDB_BUCKET, org=INFLUXDB_ORG, record=point)
        
        if (i + 1) % 100 == 0:
            print(f"  Kreirano {i + 1}/{count} transakcija")
    
    print(f"Uspešno kreirano {count} transakcija")


def generate_penali(write_api, count: int):
    """Generiše penale"""
    print(f"Generisanje {count} penala...")
    
    for i in range(count):
        timestamp = random_datetime(DATUM_START, DATUM_END)
        status = weighted_choice(PENAL_STATUSI)
        ugovor_id = random.randint(1, 200)
        
        # Većina penala je manjeg iznosa, ali ima i rizičnih (>5000)
        if random.random() < 0.3:  # 30% šanse za rizični penal
            iznos = round(random.uniform(5000, PENAL_IZNOS_MAX), 2)
        else:
            iznos = round(random.uniform(PENAL_IZNOS_MIN, 5000), 2)
        
        razlog = random.choice(PENAL_RAZLOZI)
        
        # Kreiranje Point objekta
        point = Point("dogadjaji") \
            .tag("tip_dogadjaja", "penal") \
            .tag("status", status) \
            .tag("entitet_id", str(ugovor_id)) \
            .field("iznos", iznos) \
            .field("opis", razlog) \
            .time(timestamp)
        
        # Upis u InfluxDB
        write_api.write(bucket=INFLUXDB_BUCKET, org=INFLUXDB_ORG, record=point)
        
        if (i + 1) % 100 == 0:
            print(f"Kreirano {i + 1}/{count} penala")
    
    print(f"Uspešno kreirano {count} penala")


def main():
    """Glavna funkcija"""
    print("SEED SKRIPTA ZA INFLUXDB - FINANSIJSKI DOGAĐAJI")

    print(f"URL: {INFLUXDB_URL}")
    print(f"Organizacija: {INFLUXDB_ORG}")
    print(f"Bucket: {INFLUXDB_BUCKET}")
    print(f"Period: {DATUM_START.strftime('%Y-%m-%d')} - {DATUM_END.strftime('%Y-%m-%d')}")
    
    try:
        # Konekcija sa InfluxDB
        print("\nUspostavljanje konekcije sa InfluxDB...")
        client = InfluxDBClient(
            url=INFLUXDB_URL,
            token=INFLUXDB_TOKEN,
            org=INFLUXDB_ORG
        )
        
        # Write API
        write_api = client.write_api(write_options=SYNCHRONOUS)
        
        print("Konekcija uspešno uspostavljena\n")
        
        # Generisanje podataka
        generate_transakcije(write_api, BROJ_TRANSAKCIJA)
        print()
        generate_penali(write_api, BROJ_PENALA)
        
        # Zatvaranje konekcije
        client.close()
        
        print("\n" + "=" * 60)
        print("USPEŠNO ZAVRŠENO!")
        print(f"Ukupno kreirano: {BROJ_TRANSAKCIJA + BROJ_PENALA} događaja")
        print(f"Transakcije: {BROJ_TRANSAKCIJA}")
        print(f"Penali: {BROJ_PENALA}")
        
    except Exception as e:
        print(f"\nGREŠKA: {str(e)}")
        raise


if __name__ == "__main__":
    main()
