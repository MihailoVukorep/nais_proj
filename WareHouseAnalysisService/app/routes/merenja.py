from fastapi import APIRouter, HTTPException, Query
from datetime import datetime
from app.services.influx_service import influx_service
from typing import List, Optional
import logging

router = APIRouter(prefix="/merenja", tags=["Merenja Temperature i Vlažnosti"])
logger = logging.getLogger(__name__)


# ===== TEMPERATURA MERENJA =====

@router.post("/temperatura", response_model=dict, status_code=201)
async def kreiraj_merenje_temperature(
    skladiste_id: int = Query(..., ge=1, le=5, description="ID skladišta (1-5)"),
    temperatura: float = Query(..., description="Temperatura u °C"),
    senzor_id: str = Query(..., description="ID senzora"),
    lokacija: str = Query(..., description="Lokacija u skladištu")
):
    """CREATE - Kreira novo merenje temperature"""
    try:
        influx_service.write_merenje_temperature(
            skladiste_id=skladiste_id,
            temperatura=temperatura,
            senzor_id=senzor_id,
            lokacija=lokacija
        )
        return {
            "message": "Merenje temperature uspešno kreirano",
            "skladiste_id": skladiste_id,
            "temperatura": temperatura,
            "senzor_id": senzor_id,
            "status": influx_service._determine_temperature_status(temperatura)
        }
    except Exception as e:
        logger.error(f"Greška pri kreiranju merenja temperature: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/temperatura", response_model=List[dict])
async def dohvati_merenja_temperature(
    skladiste_id: Optional[int] = Query(None, ge=1, le=5, description="ID skladišta (1-5)"),
    limit: int = Query(100, ge=1, le=1000, description="Maksimalan broj rezultata")
):
    """READ - Vraća merenja temperature"""
    try:
        skladiste_filter = f'|> filter(fn: (r) => r.skladiste_id == "{skladiste_id}")' if skladiste_id else ""
        
        flux_query = f'''
from(bucket: "{influx_service.bucket}")
  |> range(start: -30d)
  |> filter(fn: (r) => r._measurement == "merenja_temperatura")
  |> filter(fn: (r) => r._field == "vrednost")
  {skladiste_filter}
  |> sort(columns: ["_time"], desc: true)
  |> limit(n: {limit})
  |> yield(name: "temperatura_merenja")
        '''
        
        result = influx_service.query_api.query(flux_query, org=influx_service.org)
        data = []
        
        for table in result:
            for record in table.records:
                data.append({
                    "timestamp": record.get_time(),
                    "skladiste_id": int(record.values.get('skladiste_id')),
                    "temperatura": record.get_value(),
                    "senzor_id": record.values.get('senzor_id'),
                    "lokacija": record.values.get('lokacija'),
                    "status": record.values.get('status')
                })
        
        return data
    except Exception as e:
        logger.error(f"Greška pri dohvatanju merenja temperature: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.delete("/temperatura/{skladiste_id}/{senzor_id}", response_model=dict)
async def obrisi_merenje_temperature(
    skladiste_id: int,
    senzor_id: str,
    timestamp: str = Query(..., description="Timestamp merenja za brisanje (ISO format)")
):
    """DELETE - Briše merenje temperature"""
    try:
        timestamp_dt = datetime.fromisoformat(timestamp.replace('Z', '+00:00'))
        
        success = influx_service.delete_merenje_temperature(skladiste_id, senzor_id, timestamp_dt)
        
        if not success:
            raise HTTPException(status_code=404, detail="Merenje nije pronađeno")
        
        return {
            "message": "Merenje temperature uspešno obrisano",
            "skladiste_id": skladiste_id,
            "senzor_id": senzor_id,
            "timestamp": timestamp
        }
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Greška pri brisanju merenja temperature: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


# ===== VLAŽNOST MERENJA =====

@router.post("/vlaznost", response_model=dict, status_code=201)
async def kreiraj_merenje_vlaznosti(
    skladiste_id: int = Query(..., ge=1, le=5, description="ID skladišta (1-5)"),
    vlaznost: float = Query(..., ge=0, le=100, description="Vlažnost u %"),
    senzor_id: str = Query(..., description="ID senzora"),
    lokacija: str = Query(..., description="Lokacija u skladištu")
):
    """CREATE - Kreira novo merenje vlažnosti"""
    try:
        influx_service.write_merenje_vlaznost(
            skladiste_id=skladiste_id,
            vlaznost=vlaznost,
            senzor_id=senzor_id,
            lokacija=lokacija
        )
        return {
            "message": "Merenje vlažnosti uspešno kreirano",
            "skladiste_id": skladiste_id,
            "vlaznost": vlaznost,
            "senzor_id": senzor_id,
            "status": influx_service._determine_humidity_status(vlaznost)
        }
    except Exception as e:
        logger.error(f"Greška pri kreiranju merenja vlažnosti: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/vlaznost", response_model=List[dict])
async def dohvati_merenja_vlaznosti(
    skladiste_id: Optional[int] = Query(None, ge=1, le=5, description="ID skladišta (1-5)"),
    limit: int = Query(100, ge=1, le=1000, description="Maksimalan broj rezultata")
):
    """READ - Vraća merenja vlažnosti"""
    try:
        skladiste_filter = f'|> filter(fn: (r) => r.skladiste_id == "{skladiste_id}")' if skladiste_id else ""
        
        flux_query = f'''
from(bucket: "{influx_service.bucket}")
  |> range(start: -30d)
  |> filter(fn: (r) => r._measurement == "merenja_vlaznost")
  |> filter(fn: (r) => r._field == "vrednost")
  {skladiste_filter}
  |> sort(columns: ["_time"], desc: true)
  |> limit(n: {limit})
  |> yield(name: "vlaznost_merenja")
        '''
        
        result = influx_service.query_api.query(flux_query, org=influx_service.org)
        data = []
        
        for table in result:
            for record in table.records:
                data.append({
                    "timestamp": record.get_time(),
                    "skladiste_id": int(record.values.get('skladiste_id')),
                    "vlaznost": record.get_value(),
                    "senzor_id": record.values.get('senzor_id'),
                    "lokacija": record.values.get('lokacija'),
                    "status": record.values.get('status')
                })
        
        return data
    except Exception as e:
        logger.error(f"Greška pri dohvatanju merenja vlažnosti: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.delete("/vlaznost/{skladiste_id}/{senzor_id}", response_model=dict)
async def obrisi_merenje_vlaznosti(
    skladiste_id: int,
    senzor_id: str,
    timestamp: str = Query(..., description="Timestamp merenja za brisanje (ISO format)")
):
    """DELETE - Briše merenje vlažnosti"""
    try:
        timestamp_dt = datetime.fromisoformat(timestamp.replace('Z', '+00:00'))
        
        success = influx_service.delete_merenje_vlaznost(skladiste_id, senzor_id, timestamp_dt)
        
        if not success:
            raise HTTPException(status_code=404, detail="Merenje nije pronađeno")
        
        return {
            "message": "Merenje vlažnosti uspešno obrisano",
            "skladiste_id": skladiste_id,
            "senzor_id": senzor_id,
            "timestamp": timestamp
        }
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Greška pri brisanju merenja vlažnosti: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


# ===== SLOŽENI UPITI =====

@router.get("/analize/dnevne-statistike", response_model=List[dict])
async def dnevne_statistike_po_skladistu(
    days: int = Query(30, ge=1, le=365, description="Broj dana unazad")
):
    """
    SLOŽEN UPIT 1: Dnevne statistike po skladištu
    Kombinuje filtriranje + grupisanje + agregaciju + sortiranje
    """
    try:
        rezultat = influx_service.query_complex_1_daily_stats_by_warehouse(days)
        return rezultat
    except Exception as e:
        logger.error(f"Greška pri dnevnim statistikama: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/analize/kriticni-uslovi-agregacija", response_model=List[dict])
async def kriticni_uslovi_agregacija(
    days: int = Query(7, ge=1, le=30, description="Broj dana unazad")
):
    """
    SLOŽEN UPIT 2: Agregacija kritičnih uslova po skladištu
    Kombinuje filtriranje + grupisanje + agregaciju + sortiranje
    """
    try:
        rezultat = influx_service.query_complex_2_critical_conditions_aggregated(days)
        return rezultat
    except Exception as e:
        logger.error(f"Greška pri agregaciji kritičnih uslova: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/analize/senzori-ranking", response_model=List[dict])
async def ranking_performansi_senzora(
    days: int = Query(14, ge=1, le=90, description="Broj dana unazad")
):
    """
    SLOŽEN UPIT 3: Ranking performansi senzora
    Kombinuje filtriranje + grupisanje + agregaciju + sortiranje
    """
    try:
        rezultat = influx_service.query_complex_3_sensor_performance_ranking(days)
        return rezultat
    except Exception as e:
        logger.error(f"Greška pri ranking senzora: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))