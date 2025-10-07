from fastapi import APIRouter, HTTPException, Query
from fastapi.responses import FileResponse
from typing import Optional, Literal
from app.models.schemas import IzvestajParametri, IzvestajResponse
from app.services.report_service import report_service
from datetime import datetime
import logging
import os

router = APIRouter(prefix="/izvestaji", tags=["Izveštaji"])
logger = logging.getLogger(__name__)


@router.post("/generiši", response_model=IzvestajResponse)
async def generiši_izvestaj(parametri: IzvestajParametri):
    """
    Generiše kompletan PDF izveštaj o finansijskim događajima.
    
    **Izveštaj sadrži:**
    
    1. **PROSTA SEKCIJA 1 - Pregled transakcija:**
       - Filtriranje po statusu (na_cekanju, uspesna, neuspesna)
       - Filtriranje po iznosu (min/max)
       - Tabelarni prikaz sa statistikom
    
    2. **PROSTA SEKCIJA 2 - Pregled penala:**
       - Filtriranje po statusu (kreiran, placen)
       - Filtriranje po minimalnom iznosu
       - Tabelarni prikaz sa statistikom
    
    3. **SLOŽENA SEKCIJA - Kompleksna analiza:**
       - **Dnevni promet:** Agregacija uspešnih transakcija po danima (sum)
       - **Uporedna analiza:** Grupisanje penala i transakcija po nedeljama
       - **Rizični penali:** Identifikacija i agregacija po ugovorima
       - Grafikoni za svaki deo analize
    
    **Parametri:**
    - `transakcije_status`: Filter statusa za transakcije
    - `transakcije_min_iznos`, `transakcije_max_iznos`: Opseg iznosa
    - `penali_status`: Filter statusa za penale
    - `penali_min_iznos`: Minimalni iznos penala
    - `dnevni_promet_days`: Period za analizu prometa (1-365 dana)
    - `uporedna_analiza_months`: Period za uporednu analizu (1-12 meseci)
    - `limit`: Maksimalan broj zapisa u prostim sekcijama (1-500)
    """
    try:
        logger.info(f"Generisanje izveštaja sa parametrima: {parametri.dict()}")
        
        # Generisanje PDF izveštaja
        pdf_path = report_service.generate_full_report(
            transakcije_status=parametri.transakcije_status,
            transakcije_min_iznos=parametri.transakcije_min_iznos,
            transakcije_max_iznos=parametri.transakcije_max_iznos,
            penali_status=parametri.penali_status,
            penali_min_iznos=parametri.penali_min_iznos,
            dnevni_promet_days=parametri.dnevni_promet_days,
            uporedna_analiza_months=parametri.uporedna_analiza_months,
            limit=parametri.limit
        )
        
        if not os.path.exists(pdf_path):
            raise HTTPException(status_code=500, detail="Greška pri generisanju PDF izveštaja")
        
        file_name = os.path.basename(pdf_path)
        
        return IzvestajResponse(
            message="Izveštaj uspešno generisan",
            pdf_url=f"/api/izvestaji/preuzmi/{file_name}",
            file_name=file_name,
            timestamp=datetime.now()
        )
        
    except Exception as e:
        logger.error(f"Greška pri generisanju izveštaja: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Greška pri generisanju izveštaja: {str(e)}")


@router.get("/preuzmi/{file_name}")
async def preuzmi_izvestaj(file_name: str):
    """
    Preuzima generisani PDF izveštaj.
    
    **Upotreba:**
    1. Prvo pozovite `/generiši` endpoint da kreirate izveštaj
    2. Koristite `pdf_url` iz odgovora da preuzmete fajl
    
    **Primer:**
    ```
    GET /api/izvestaji/preuzmi/izvestaj_finansijski_dogadjaji_20241006_123045.pdf
    ```
    """
    try:
        pdf_path = os.path.join(report_service.reports_dir, file_name)
        
        if not os.path.exists(pdf_path):
            raise HTTPException(status_code=404, detail="Izveštaj nije pronađen")
        
        return FileResponse(
            path=pdf_path,
            media_type='application/pdf',
            filename=file_name
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Greška pri preuzimanju izveštaja: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/quick-generate")
async def generiši_brzi_izvestaj(
    transakcije_status: Optional[Literal["na_cekanju", "uspesna", "neuspesna"]] = Query(None),
    penali_status: Optional[Literal["kreiran", "placen"]] = Query(None),
    days: int = Query(30, ge=1, le=365, description="Broj dana za analizu"),
    limit: int = Query(50, ge=1, le=500, description="Maksimalan broj zapisa")
):
    """
    Brzo generisanje izveštaja sa osnovnim parametrima (GET metod).
    
    **Upotreba:**
    Za brze testove i jednostavna generisanja bez slanja JSON payload-a.
    
    **Primer:**
    ```
    GET /api/izvestaji/quick-generate?transakcije_status=uspesna&days=7&limit=20
    ```
    
    **Vraća:** Direktno PDF fajl za download.
    """
    try:
        logger.info(f"Brzo generisanje izveštaja: status_t={transakcije_status}, status_p={penali_status}, days={days}")
        
        pdf_path = report_service.generate_full_report(
            transakcije_status=transakcije_status,
            penali_status=penali_status,
            dnevni_promet_days=days,
            uporedna_analiza_months=3,
            limit=limit
        )
        
        if not os.path.exists(pdf_path):
            raise HTTPException(status_code=500, detail="Greška pri generisanju PDF izveštaja")
        
        file_name = os.path.basename(pdf_path)
        
        return FileResponse(
            path=pdf_path,
            media_type='application/pdf',
            filename=file_name
        )
        
    except Exception as e:
        logger.error(f"Greška pri brzom generisanju izveštaja: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/lista")
async def lista_izvestaja():
    """
    Vraća listu svih generisanih izveštaja.
    
    **Vraća:**
    - Lista dostupnih PDF fajlova
    - Za svaki fajl: ime, veličina, datum kreiranja, URL za preuzimanje
    """
    try:
        if not os.path.exists(report_service.reports_dir):
            return {"izvestaji": []}
        
        files = []
        for filename in os.listdir(report_service.reports_dir):
            if filename.endswith('.pdf'):
                file_path = os.path.join(report_service.reports_dir, filename)
                file_stats = os.stat(file_path)
                
                files.append({
                    "file_name": filename,
                    "size_mb": round(file_stats.st_size / (1024 * 1024), 2),
                    "created_at": datetime.fromtimestamp(file_stats.st_ctime).isoformat(),
                    "download_url": f"/api/izvestaji/preuzmi/{filename}"
                })
        
        # Sortiraj po datumu kreiranja (najnoviji prvi)
        files.sort(key=lambda x: x['created_at'], reverse=True)
        
        return {
            "ukupno": len(files),
            "izvestaji": files
        }
        
    except Exception as e:
        logger.error(f"Greška pri listanju izveštaja: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.delete("/obrisi/{file_name}")
async def obrisi_izvestaj(file_name: str):
    """
    Briše generisani PDF izveštaj.
    
    **Napomena:**
    Briše samo PDF fajlove sa ekstenzijom `.pdf` iz `reports/` direktorijuma.
    """
    try:
        # Sigurnosna provera - samo PDF fajlovi
        if not file_name.endswith('.pdf'):
            raise HTTPException(status_code=400, detail="Možete brisati samo PDF fajlove")
        
        pdf_path = os.path.join(report_service.reports_dir, file_name)
        
        if not os.path.exists(pdf_path):
            raise HTTPException(status_code=404, detail="Izveštaj nije pronađen")
        
        os.remove(pdf_path)
        
        # Briši i povezane slike grafikona ako postoje
        chart_prefix = file_name.replace('.pdf', '').replace('izvestaj_finansijski_dogadjaji_', 'chart_')
        for filename in os.listdir(report_service.reports_dir):
            if filename.startswith('chart_') and chart_prefix in filename:
                chart_path = os.path.join(report_service.reports_dir, filename)
                try:
                    os.remove(chart_path)
                except:
                    pass
        
        logger.info(f"Izveštaj obrisan: {file_name}")
        
        return {
            "message": "Izveštaj uspešno obrisan",
            "file_name": file_name
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Greška pri brisanju izveštaja: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))
