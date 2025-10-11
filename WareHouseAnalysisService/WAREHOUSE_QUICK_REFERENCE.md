# Analiza Skladišnih Uslova - Kratki vodič

## Šta radi ovaj mikroservis?

Ovaj mikroservis prati **temperaturu** i **vlažnost** u skladištima i pomaže da održimo optimalnu kvalitetu čuvanja robe.

**Framework**: Flask + Flasgger (Swagger) + InfluxDB

## Optimalni uslovi

### Temperatura
- **Optimalno**: 18-24°C
- **Rizično**: <18°C ili >24°C  
- **Kritično**: <10°C ili >35°C

### Vlažnost
- **Optimalno**: 40-60%
- **Rizično**: <40% ili >60%
- **Kritično**: <20% ili >80%

## API Endpoints

### 1. Kreiraj merenje temperature
```http
POST /api/merenja/temperatura?skladiste_id=1&temperatura=22.5&senzor_id=TEMP_01&lokacija=Zona A
```

### 2. Kreiraj merenje vlažnosti
```http
POST /api/merenja/vlaznost?skladiste_id=1&vlaznost=55.0&senzor_id=HUMID_01&lokacija=Zona A
```

### 3. Vidi merenja temperature
```http
GET /api/merenja/temperatura?skladiste_id=1&limit=100
```

### 4. Vidi merenja vlažnosti
```http
GET /api/merenja/vlaznost?skladiste_id=1&limit=100
```

### 5. Obriši merenje temperature
```http
DELETE /api/merenja/temperatura/1/TEMP_01?timestamp=2024-01-01T12:00:00Z
```

### 6. Obriši merenje vlažnosti
```http
DELETE /api/merenja/vlaznost/1/HUMID_01?timestamp=2024-01-01T12:00:00Z
```

## Složeni upiti i analize

### 1. Dnevne statistike po skladištu
```http
GET /api/merenja/analize/dnevne-statistike?days=30
```

### 2. Agregacija kritičnih uslova
```http
GET /api/merenja/analize/kriticni-uslovi-agregacija?days=7
```

### 3. Ranking performansi senzora
```http
GET /api/merenja/analize/senzori-ranking?days=14
```

## Pokretanje

### Lokalno sa Flask
```bash
cd WareHouseAnalysisService
pip install -r requirements.txt

# Postavi environment varijable
$env:FLASK_APP="app.main"
$env:FLASK_ENV="development"

# Pokreni Flask aplikaciju
python -m flask run --host=0.0.0.0 --port=8002
```

### Docker
```bash
docker-compose up -d
```

**Pristup aplikaciji**: http://localhost:8002  
**Swagger dokumentacija**: http://localhost:8002/apidocs/

## Environment varijable

```env
INFLUXDB_URL=http://localhost:8087
INFLUXDB_TOKEN=warehouse-super-secret-auth-token
INFLUXDB_ORG=IIS_SUDPI
INFLUXDB_BUCKET=skladisni_uslovi
SECRET_KEY=dev-secret-key
HOST=0.0.0.0
PORT=8002
```

## Test podaci

Za dodavanje test podataka:
```bash
cd scripts
python seed_data.py
```

Ovo će kreirati 60 dana merenja za 5 različitih skladišta (1000 temperatura + 1000 vlažnosti merenja).

## Flask vs FastAPI

Ovaj servis je konvertovan sa **FastAPI** na **Flask + Flasgger**:
- ✅ Flask aplikacija sa Blueprint arhitekturom
- ✅ Flasgger za Swagger UI dokumentaciju  
- ✅ Flask-CORS za cross-origin zahteve
- ✅ Sinhronni request handling
- ✅ Manual validacija parametara
- ✅ Dataclass modeli umesto Pydantic