from fastapi import APIRouter, HTTPException, Query
from datetime import datetime
from app.models.schemas import (
    DogadjajCreate, DogadjajResponse, 
    TransakcijaCreate, TransakcijaUpdate,
    PenalCreate, PenalUpdate
)
from app.services.influx_service import influx_service
from typing import List
import logging

router = APIRouter(prefix="/dogadjaji", tags=["Događaji"])
logger = logging.getLogger(__name__)


@router.post("/transakcija", response_model=dict, status_code=201)
async def kreiraj_transakciju(transakcija: TransakcijaCreate):
    """CREATE - Kreira novu transakciju"""
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
        logger.error(f"Greška pri kreiranju transakcije: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/transakcija", response_model=List[dict])
async def dohvati_sve_transakcije(limit: int = Query(100, ge=1, le=1000)):
    """READ - Vraća sve transakcije"""
    try:
        transakcije = influx_service.get_dogadjaji_by_type("transakcija", limit=limit)
        return transakcije
    except Exception as e:
        logger.error(f"Greška pri dohvatanju transakcija: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/transakcija/{faktura_id}", response_model=dict)
async def dohvati_transakciju(faktura_id: int):
    """READ - Vraća jednu transakciju po faktura_id"""
    try:
        transakcija = influx_service.get_dogadjaj_by_id("transakcija", faktura_id)
        if not transakcija:
            raise HTTPException(status_code=404, detail=f"Transakcija sa faktura_id={faktura_id} nije pronađena")
        return transakcija
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Greška pri dohvatanju transakcije: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.put("/transakcija/{faktura_id}", response_model=dict)
async def azuriraj_transakciju(faktura_id: int, transakcija: TransakcijaUpdate):
    """UPDATE - Ažurira postojeću transakciju"""
    try:
        # Pripremi opis ako se menja potvrda ili opis
        novi_opis = None
        if transakcija.potvrda is not None or transakcija.opis is not None:
            # Dohvati postojeći zapis da bi spojio potvrdu i opis
            postojeca = influx_service.get_dogadjaj_by_id("transakcija", faktura_id)
            if not postojeca:
                raise HTTPException(status_code=404, detail=f"Transakcija sa faktura_id={faktura_id} nije pronađena")
            
            # Parsiranje postojećeg opisa
            stari_opis = postojeca.get('opis', '')
            stara_potvrda = ''
            if stari_opis.startswith('Potvrda: '):
                parts = stari_opis.split('. ', 1)
                stara_potvrda = parts[0].replace('Potvrda: ', '')
                stari_text = parts[1] if len(parts) > 1 else ''
            else:
                stari_text = stari_opis
            
            nova_potvrda = transakcija.potvrda if transakcija.potvrda is not None else stara_potvrda
            novi_text = transakcija.opis if transakcija.opis is not None else stari_text
            novi_opis = f"Potvrda: {nova_potvrda}. {novi_text}"
        
        success = influx_service.update_dogadjaj(
            tip_dogadjaja="transakcija",
            entitet_id=faktura_id,
            status=transakcija.status,
            iznos=transakcija.iznos,
            opis=novi_opis
        )
        
        if not success:
            raise HTTPException(status_code=404, detail=f"Transakcija sa faktura_id={faktura_id} nije pronađena")
        
        return {
            "message": "Transakcija uspešno ažurirana",
            "faktura_id": faktura_id
        }
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Greška pri ažuriranju transakcije: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.delete("/transakcija/{faktura_id}", response_model=dict)
async def obrisi_transakciju(faktura_id: int):
    """DELETE - Briše transakciju po faktura_id"""
    try:
        success = influx_service.delete_dogadjaj_by_id("transakcija", faktura_id)
        if not success:
            raise HTTPException(status_code=404, detail=f"Transakcija sa faktura_id={faktura_id} nije pronađena")
        
        return {
            "message": "Transakcija uspešno obrisana",
            "faktura_id": faktura_id
        }
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Greška pri brisanju transakcije: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/penal", response_model=dict, status_code=201)
async def kreiraj_penal(penal: PenalCreate):
    """CREATE - Kreira novi penal"""
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
        logger.error(f"Greška pri kreiranju penala: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/penal", response_model=List[dict])
async def dohvati_sve_penale(limit: int = Query(100, ge=1, le=1000)):
    """READ - Vraća sve penale"""
    try:
        penali = influx_service.get_dogadjaji_by_type("penal", limit=limit)
        return penali
    except Exception as e:
        logger.error(f"Greška pri dohvatanju penala: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/penal/{ugovor_id}", response_model=dict)
async def dohvati_penal(ugovor_id: int):
    """READ - Vraća jedan penal po ugovor_id"""
    try:
        penal = influx_service.get_dogadjaj_by_id("penal", ugovor_id)
        if not penal:
            raise HTTPException(status_code=404, detail=f"Penal sa ugovor_id={ugovor_id} nije pronađen")
        return penal
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Greška pri dohvatanju penala: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.put("/penal/{ugovor_id}", response_model=dict)
async def azuriraj_penal(ugovor_id: int, penal: PenalUpdate):
    """UPDATE - Ažurira postojeći penal"""
    try:
        success = influx_service.update_dogadjaj(
            tip_dogadjaja="penal",
            entitet_id=ugovor_id,
            status=penal.status,
            iznos=penal.iznos,
            opis=penal.razlog
        )
        
        if not success:
            raise HTTPException(status_code=404, detail=f"Penal sa ugovor_id={ugovor_id} nije pronađen")
        
        return {
            "message": "Penal uspešno ažuriran",
            "ugovor_id": ugovor_id
        }
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Greška pri ažuriranju penala: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.delete("/penal/{ugovor_id}", response_model=dict)
async def obrisi_penal(ugovor_id: int):
    """DELETE - Briše penal po ugovor_id"""
    try:
        success = influx_service.delete_dogadjaj_by_id("penal", ugovor_id)
        if not success:
            raise HTTPException(status_code=404, detail=f"Penal sa ugovor_id={ugovor_id} nije pronađen")
        
        return {
            "message": "Penal uspešno obrisan",
            "ugovor_id": ugovor_id
        }
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Greška pri brisanju penala: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/", response_model=dict, status_code=201)
async def kreiraj_dogadjaj(dogadjaj: DogadjajCreate):
    """Generički endpoint - Kreira novi finansijski događaj"""
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


@router.post("/compensate", response_model=dict)
async def compensate_dogadjaj(
    tip_dogadjaja: str = Query(..., description="Tip događaja: transakcija ili penal"),
    entitet_id: int = Query(..., description="ID entiteta (faktura_id ili ugovor_id)")
):
    """
    KOMPENZACIONI ENDPOINT ZA SAGA PATTERN
    
    Briše događaj iz InfluxDB (rollback operacija).
    
    Koristi se kada Oracle transakcija nije uspela i potreban je rollback InfluxDB zapisa.
    
    POST /api/dogadjaji/compensate?tip_dogadjaja=transakcija&entitet_id=123
    """
    try:
        logger.info(f"KOMPENZACIJA: Brisanje {tip_dogadjaja} sa entitet_id={entitet_id}")
        
        success = influx_service.delete_dogadjaj_by_id(tip_dogadjaja, entitet_id)
        
        if not success:
            logger.warning(f"Nije pronađen događaj za kompenzaciju: {tip_dogadjaja}/{entitet_id}")
            return {
                "message": "Događaj nije pronađen (možda već obrisan)",
                "compensated": False,
                "tip_dogadjaja": tip_dogadjaja,
                "entitet_id": entitet_id
            }
        
        logger.info(f"KOMPENZACIJA USPEŠNA: {tip_dogadjaja}/{entitet_id}")
        return {
            "message": "Kompenzacija uspešna - događaj obrisan iz InfluxDB",
            "compensated": True,
            "tip_dogadjaja": tip_dogadjaja,
            "entitet_id": entitet_id
        }
        
    except Exception as e:
        logger.error(f"Greška pri kompenzaciji: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Kompenzacija neuspešna: {str(e)}")