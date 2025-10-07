"""
Skriptа za brisanje svih merenja iz InfluxDB bucket-a `skladisni_uslovi`.
Koristi ENV varijable iz .env ili podrazumevane vrednosti.

Pokretanje:
    & .\venv\Scripts\Activate.ps1
    python scripts\delete_all_measurements.py
"""

import os
from dotenv import load_dotenv
from influxdb_client import InfluxDBClient
from datetime import datetime, timezone

load_dotenv()

INFLUXDB_URL = os.getenv("INFLUXDB_URL", "http://localhost:8087")
INFLUXDB_TOKEN = os.getenv("INFLUXDB_TOKEN", "warehouse-super-secret-auth-token")
INFLUXDB_ORG = os.getenv("INFLUXDB_ORG", "IIS_SUDPI")
INFLUXDB_BUCKET = os.getenv("INFLUXDB_BUCKET", "skladisni_uslovi")

print("Povezivanje na InfluxDB:", INFLUXDB_URL)
client = InfluxDBClient(url=INFLUXDB_URL, token=INFLUXDB_TOKEN, org=INFLUXDB_ORG)
delete_api = client.delete_api()

start = "1970-01-01T00:00:00Z"
stop = datetime.now(timezone.utc).isoformat()

measurements = ["merenja_temperatura", "merenja_vlaznost"]

for m in measurements:
    predicate = f'_measurement="{m}"'
    print(f"Brisanje merenja: {m} (predicate: {predicate})")
    try:
        delete_api.delete(start, stop, predicate=predicate, bucket=INFLUXDB_BUCKET, org=INFLUXDB_ORG)
        print(f"  OK: zatraženo brisanje za {m}")
    except Exception as e:
        print(f"  GREŠKA pri brisanju {m}: {e}")

client.close()
print("Gotovo.")
