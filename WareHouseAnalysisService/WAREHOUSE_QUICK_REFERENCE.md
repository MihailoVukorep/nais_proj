# Analiza Skladišnih Uslova - Kratki vodič

## Šta radi ovaj mikroservis?

Ovaj mikroservis prati **temperaturu** i **vlažnost** u skladištima i pomaže da održimo optimalnu kvalitetu čuvanja robe.

## Optimalni uslovi

### Temperatura
- **Optimalno**: 18-24°C
- **Rizično**: <18°C ili >24°C  
- **Kritično**: <10°C ili >35°C

### Vlažnost
- **Optimalno**: 40-60%
- **Rizično**: <40% ili >60%
- **Kritično**: <20% ili >80%

## Osnovne operacije

### 1. Dodaj novo merenje
```http
POST /api/uslovi/
{
  "skladiste_id": 1,
  "temperatura": 22.5,
  "vlaznost": 55.0,
  "senzor_id": "TEMP_01",
  "lokacija": "Zona A"
}
```

### 2. Vidi trenutne uslove
```http
GET /api/uslovi/?limit=50
```

### 3. Vidi uslove u skladištu
```http
GET /api/uslovi/skladiste/1?hours=24
```

### 4. Ažuriraj temperaturu
```http
PUT /api/uslovi/temperatura/1/TEMP_01
{
  "temperatura": 21.0
}
```

## Analize

### Dnevni pregled
```http
GET /api/analize/dnevni-uslovi?days=30&skladiste_id=1
```

### Kritični uslovi
```http
GET /api/analize/kriticni-uslovi?days=7
```

### Preporuke za optimizaciju
```http
GET /api/analize/optimizacija?skladiste_id=1&days=7
```

## Pokretanje

### Lokalno
```bash
cd WareHouseAnalysisService
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8002 --reload
```

### Docker
```bash
docker-compose up -d
```

Pristup: http://localhost:8002/docs

## Test podaci

Za dodavanje test podataka:
```bash
cd scripts
python seed_data.py
```

Ovo će kreirati 30 dana merenja za 3 različita skladišta.