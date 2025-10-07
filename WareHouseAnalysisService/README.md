# WareHouse Analysis Service

Mikroservis za analizu skladišnih uslova koji prati i analizira temperaturu i vlažnost u skladištima kako bi identifikovao optimalne i kritične uslove čuvanja robe.

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

```bash
# Kloniranje i setup
cd WareHouseAnalysisService

# Docker pokretanje (preporučeno)
docker-compose up -d

# Dodavanje test podataka (1000 temp + 1000 vlažnost po skladištu)
cd scripts
python seed_data.py
```

**Pristup:**
- API: http://localhost:8002/docs
- InfluxDB: http://localhost:8087

## 📈 Test podaci

Seed skripta generiše:
- **5000 merenja temperature** (1000 po skladištu 1-5)
- **5000 merenja vlažnosti** (1000 po skladištu 1-5)  
- **Ukupno: 10000 merenja** u 60-dnevnom periodu
- **Senzori**: Realistički ID-jevi po skladištu
- **Lokacije**: Različite zone/sektori po skladištu

## 🔗 Integracija sa Oracle

Pripreman za integraciju sa Oracle bazom koja sadrži:
- `Artikal`, `Zalihe`, `Skladiste`, `Temperatura` tabele
- Transakcijske operacije između InfluxDB i Oracle
- SAGA pattern za konzistentnost podataka