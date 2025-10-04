from fastapi import APIRouter, HTTPException, Query
from typing import List
from app.models.schemas import (
    DnevniPrometResponse, 
    RizicniPenalResponse, 
    NedeljnaAnalizaResponse
)
from app.services.influx_service import influx_service
import logging

router = APIRouter(prefix="/analize", tags=["Analize"])
logger = logging.getLogger(__name__)


@router.get("/dnevni-promet", response_model=List[DnevniPrometResponse])
async def dnevni_promet(days: int = Query(30, ge=1, le=365)):
    """
    Složen upit 1: Dnevni promet - Agregacija i grupisanje
    Vraća ukupnu sumu uspešnih transakcija po danima
    """
    try:
        rezultat = influx_service.query_dnevni_promet(days)
        return rezultat
    except Exception as e:
        logger.error(f"Greška: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/rizicni-penali", response_model=List[RizicniPenalResponse])
async def rizicni_penali(
    min_iznos: float = Query(5000, ge=0),
    limit: int = Query(10, ge=1, le=100)
):
    """
    Složen upit 2: Rizični penali - Filtriranje i sortiranje
    Vraća najnovije kreirane penale čiji je iznos veći od zadatog
    """
    try:
        rezultat = influx_service.query_rizicni_penali(min_iznos, limit)
        return rezultat
    except Exception as e:
        logger.error(f"Greška: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/uporedna-analiza", response_model=List[NedeljnaAnalizaResponse])
async def uporedna_analiza(months: int = Query(3, ge=1, le=12)):
    """
    Složen upit 3: Uporedna analiza - Kombinacija svega
    Vraća broj penala i transakcija po nedeljama
    """
    try:
        rezultat = influx_service.query_uporedna_analiza(months)
        return rezultat
    except Exception as e:
        logger.error(f"Greška: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))