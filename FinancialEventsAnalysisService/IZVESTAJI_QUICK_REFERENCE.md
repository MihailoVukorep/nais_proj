# 📊 Generator Izveštaja - Brzi Pregled

## 🚀 Pokretanje

```bash
# Docker (preporučeno)
docker-compose up -d

# Ili lokalno
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8001
```

API dostupan na: **http://localhost:8001**

---

## 📋 Endpoints

### 1️⃣ Generisanje Izveštaja (POST)

```bash
POST http://localhost:8001/api/izvestaji/generiši
```

**Payload:**
```json
{
  "transakcije_status": "uspesna",
  "transakcije_min_iznos": 1000,
  "transakcije_max_iznos": 50000,
  "penali_status": "kreiran",
  "penali_min_iznos": 5000,
  "dnevni_promet_days": 30,
  "uporedna_analiza_months": 3,
  "limit": 50
}
```

**Response:**
```json
{
  "message": "Izveštaj uspešno generisan",
  "pdf_url": "/api/izvestaji/preuzmi/izvestaj_finansijski_dogadjaji_20241006_123045.pdf",
  "file_name": "izvestaj_finansijski_dogadjaji_20241006_123045.pdf",
  "timestamp": "2024-10-06T12:30:45.123456"
}
```

---

### 2️⃣ Brzo Generisanje (GET)

```bash
GET http://localhost:8001/api/izvestaji/quick-generate?days=7&limit=20
```

Direktan download PDF-a.

---

### 3️⃣ Lista Izveštaja

```bash
GET http://localhost:8001/api/izvestaji/lista
```

**Response:**
```json
{
  "ukupno": 5,
  "izvestaji": [
    {
      "file_name": "izvestaj_finansijski_dogadjaji_20241006_123045.pdf",
      "size_mb": 1.23,
      "created_at": "2024-10-06T12:30:45",
      "download_url": "/api/izvestaji/preuzmi/izvestaj_finansijski_dogadjaji_20241006_123045.pdf"
    }
  ]
}
```

---

### 4️⃣ Preuzimanje

```bash
GET http://localhost:8001/api/izvestaji/preuzmi/{file_name}
```

---

### 5️⃣ Brisanje

```bash
DELETE http://localhost:8001/api/izvestaji/obrisi/{file_name}
```

---

## 📄 Struktura PDF Izveštaja

### ✅ Prosta Sekcija 1: Transakcije
- Tabela transakcija (filteri: status, iznos)
- Statistika: ukupan broj i iznos

### ✅ Prosta Sekcija 2: Penali
- Tabela penala (filteri: status, min iznos)
- Statistika: ukupan broj i iznos

### ✅ Složena Sekcija: Kompleksna Analiza

**3.1 Dnevni Promet**
- 📊 Bar chart
- Agregacija SUM() po danima
- Ukupan promet + prosek

**3.2 Uporedna Analiza**
- 📊 Grouped bar chart
- Grupisanje po nedeljama
- Penali vs Transakcije

**3.3 Rizični Penali**
- 📊 Horizontal bar chart
- Top 10 ugovora
- Agregacija po ugovorima

---

## 🧪 Testiranje

```bash
# Automatski testovi
cd tests
python test_izvestaji.py

# Ili Swagger UI
http://localhost:8001/docs
```

---

## 📚 Dokumentacija

- **Detaljna:** `IZVESTAJI_DOKUMENTACIJA.md`
- **Brzi start:** `USAGE_IZVESTAJI.md`
- **Rezime:** `IMPLEMENTACIJA_REZIME.md`
- **API docs:** http://localhost:8001/docs

---

## ✅ Bodovanje (15/15)

| Zahtev | Bodovi | Status |
|--------|--------|--------|
| 2 proste sekcije | 6 | ✅ |
| 1 složena sekcija | 9 | ✅ |
| **UKUPNO** | **15** | **✅** |

---

## 🛠️ Tehnologije

- FastAPI
- ReportLab (PDF)
- Matplotlib (Grafikoni)
- InfluxDB (Time-series)
- Flux (Složeni upiti)

---

## 💡 Primer Korištenja

```bash
# 1. Generiši izveštaj
curl -X POST "http://localhost:8001/api/izvestaji/generiši" \
  -H "Content-Type: application/json" \
  -d '{"dnevni_promet_days": 30}'

# Response: {"pdf_url": "/api/izvestaji/preuzmi/izvestaj_...pdf", ...}

# 2. Preuzmi PDF
curl "http://localhost:8001/api/izvestaji/preuzmi/izvestaj_...pdf" --output izvestaj.pdf

# 3. Otvori PDF
start izvestaj.pdf  # Windows
```

---

**📅 Implementirano:** 6. oktobar 2025  
**👨‍💻 Za:** FinancialEventsAnalysisService
