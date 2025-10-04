from fastapi import APIRouter, HTTPException, Query
from datetime import datetime
from app.models.schemas import (
    DogadjajCreate, DogadjajResponse, 
    TransakcijaCreate, PenalCreate
)
from app.services.influx_service import influx_service
import logging

router = APIRouter(prefix="/dogadjaji", tags=["Događaji"])
logger = logging.getLogger(__name__)


@router.post("/", response_model=dict, status_code=201)
async def kreiraj_dogadjaj(dogadjaj: DogadjajCreate):
    """Kreira novi finansijski događaj"""
    try:
        influx_service.write_dogadjaj(
            tip_dogadjaja=dogadjaj.tip_dogadjaja,
            status=dogadjaj.status,
            entitet_id=dogadjaj.entitet_id,
            iznos=dogadjaj.iznos,
            opis=dogadjaj.opis
        )
        return {
            "message": "Događaj uspešno kreiran",
            "tip": dogadjaj.tip_dogadjaja,
            "entitet_id": dogadjaj.entitet_id
        }
    except Exception as e:
        logger.error(f"Greška: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/transakcija", response_model=dict, status_code=201)
async def kreiraj_transakciju(transakcija: TransakcijaCreate):
    """Kreira novu transakciju"""
    try:
        influx_service.write_dogadjaj(
            tip_dogadjaja="transakcija",
            status=transakcija.status,
            entitet_id=transakcija.faktura_id,
            iznos=transakcija.iznos,
            opis=f"Potvrda: {transakcija.potvrda}. {transakcija.opis}"
        )
        return {
            "message": "Transakcija uspešno kreirana",
            "faktura_id": transakcija.faktura_id,
            "status": transakcija.status
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/penal", response_model=dict, status_code=201)
async def kreiraj_penal(penal: PenalCreate):
    """Kreira novi penal"""
    try:
        influx_service.write_dogadjaj(
            tip_dogadjaja="penal",
            status=penal.status,
            entitet_id=penal.ugovor_id,
            iznos=penal.iznos,
            opis=penal.razlog
        )
        return {
            "message": "Penal uspešno kreiran",
            "ugovor_id": penal.ugovor_id,
            "status": penal.status
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.delete("/", response_model=dict)
async def obrisi_dogadjaje(
    start: str = Query(..., description="Start vreme (ISO format)"),
    end: str = Query(..., description="End vreme (ISO format)")
):
    """Briše događaje u zadatom vremenskom opsegu"""
    try:
        count = influx_service.delete_dogadjaji(start, end)
        return {
            "message": f"Obrisano {count} događaja",
            "period": f"{start} - {end}"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))