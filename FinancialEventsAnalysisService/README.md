http://localhost:8001/docs - API dokumentacija
http://localhost:8001/health - Health check
http://localhost:8086 - InfluxDB UI

## Primeri API poziva

### Analize
http://localhost:8001/api/analize/dnevni-promet
http://localhost:8001/api/analize/dnevni-promet?days=7
http://localhost:8001/api/analize/dnevni-promet?days=90

### Generator Izveštaja (Novi!)

Generisanje kompletnog PDF izveštaja:
```bash
curl -X POST "http://localhost:8001/api/izvestaji/generiši" \
  -H "Content-Type: application/json" \
  -d '{
    "transakcije_status": "uspesna",
    "dnevni_promet_days": 30,
    "limit": 50
  }'
```

Brzo generisanje:
```bash
curl "http://localhost:8001/api/izvestaji/quick-generate?days=7" --output izvestaj.pdf
```

Lista izveštaja:
```bash
curl "http://localhost:8001/api/izvestaji/lista"
```

**Detaljniju dokumentaciju pogledajte u:** `IZVESTAJI_DOKUMENTACIJA.md`

(...)
