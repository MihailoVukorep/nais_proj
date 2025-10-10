# WareHouse Analysis Service

Mikroservis za analizu skladišnih uslova koji prati i analizira temperaturu i vlažnost u skladištima kako bi identifikovao optimalne i kritične uslove čuvanja robe.

**Framework**: Flask + Flasgger (Swagger) + InfluxDB

## 📊 Specifikacije baze podataka

- **InfluxDB bucket**: `skladisni_uslovi`
- **Logičke jedinice merenja**: 
  - `merenja_temperatura` (1000+ slogova po skladištu)
  - `merenja_vlaznost` (1000+ slogova po skladištu)
- **Skladišta**: ID 1-5 (sva opšta, bez kategorija)
- **CRUD operacije**: CREATE/DELETE za pojedinačne slogove
- **Složeni upiti**: 3 upita sa filtriranjem + grupisanjem + agregacijom + sortiranjem

## 🏭 Skladišta

Svih 5 skladišta su **opšta** sa istim karakteristikama:
- **Temperatura**: optimalno 18-24°C, kritično <10°C ili >35°C  
- **Vlažnost**: optimalno 40-60%, kritično <20% ili >80%
- **Tipovi senzora**: TEMP_Sx_01, TEMP_Sx_02, HUMID_Sx_01, HUMID_Sx_02

## 🔧 API Endpoints

### Merenja Temperature
```http
POST /api/merenja/temperatura?skladiste_id=1&temperatura=22.5&senzor_id=TEMP_S1_01&lokacija=Zona A
GET /api/merenja/temperatura?skladiste_id=1&limit=100
DELETE /api/merenja/temperatura/1/TEMP_S1_01?timestamp=2023-12-01T10:00:00Z
```

### Merenja Vlažnosti  
```http
POST /api/merenja/vlaznost?skladiste_id=1&vlaznost=55.0&senzor_id=HUMID_S1_01&lokacija=Zona A
GET /api/merenja/vlaznost?skladiste_id=1&limit=100
DELETE /api/merenja/vlaznost/1/HUMID_S1_01?timestamp=2023-12-01T10:00:00Z
```

### 3 Složena upita
```http
GET /api/merenja/analize/dnevne-statistike?days=30
GET /api/merenja/analize/kriticni-uslovi-agregacija?days=7  
GET /api/merenja/analize/senzori-ranking?days=14
```

## 🚀 Pokretanje

### Flask lokalno
```bash
cd WareHouseAnalysisService
pip install -r requirements.txt

# Postavi environment varijable
$env:FLASK_APP="app.main"
$env:FLASK_ENV="development"

# Pokreni Flask
python -m flask run --host=0.0.0.0 --port=8002
```

### Docker (preporučeno)
```bash
docker-compose up -d

# Dodavanje test podataka (1000 temp + 1000 vlažnost po skladištu)
cd scripts
python seed_data.py
```

**Pristup:**
- **Flask API**: http://localhost:8002/
- **Swagger UI**: http://localhost:8002/apidocs/
- **InfluxDB**: http://localhost:8087

## 📈 Test podaci

Seed skripta generiše:
- **5000 merenja temperature** (1000 po skladištu 1-5)
- **5000 merenja vlažnosti** (1000 po skladištu 1-5)  
- **Ukupno: 10000 merenja** u 60-dnevnom periodu
- **Senzori**: Realistički ID-jevi po skladištu
- **Lokacije**: Različite zone/sektori po skladištu

## 🛠️ Tehnologije

- **Flask 3.0.0**: Web framework
- **Flasgger 0.9.7.1**: Swagger/OpenAPI dokumentacija
- **Flask-CORS 4.0.0**: Cross-origin resource sharing
- **InfluxDB 2.7**: Time-series baza podataka
- **Docker**: Kontejnerizacija
- **Python 3.11**: Runtime environment

## 🔄 Konverzija sa FastAPI

Ovaj servis je konvertovan sa **FastAPI** na **Flask**:

| Aspect | FastAPI (staro) | Flask (novo) |
|--------|----------------|--------------|
| Framework | FastAPI + Uvicorn | Flask + Werkzeug |
| Swagger | Automatski | Flasgger dekoratori |
| Validacija | Pydantic automatski | Manualna validacija |
| Rute | @router decorators | @bp.route decorators |
| Async | async/await | Sinhronno |
| Models | Pydantic BaseModel | Python dataclasses |

## 🔗 Integracija sa Oracle

Pripreman za integraciju sa Oracle bazom koja sadrži:
- `Artikal`, `Zalihe`, `Skladiste`, `Temperatura` tabele
- Transakcijske operacije između InfluxDB i Oracle
- SAGA pattern za konzistentnost podataka