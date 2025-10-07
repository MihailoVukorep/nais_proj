from pydantic import BaseModel, Field
from typing import Literal, Optional
from datetime import datetime


class DogadjajBase(BaseModel):
    """Osnovni model za finansijski događaj"""
    tip_dogadjaja: Literal["transakcija", "penal"]
    status: str
    entitet_id: int
    iznos: float
    opis: str


class DogadjajCreate(DogadjajBase):
    """Model za kreiranje novog događaja"""
    pass


class DogadjajResponse(DogadjajBase):
    """Model za odgovor sa događajem"""
    timestamp: datetime
    
    class Config:
        from_attributes = True


class TransakcijaCreate(BaseModel):
    """Model za kreiranje transakcije"""
    faktura_id: int
    iznos: float
    status: Literal["na_cekanju", "uspesna", "neuspesna"]
    potvrda: str
    opis: Optional[str] = ""


class TransakcijaUpdate(BaseModel):
    """Model za ažuriranje transakcije"""
    iznos: Optional[float] = None
    status: Optional[Literal["na_cekanju", "uspesna", "neuspesna"]] = None
    potvrda: Optional[str] = None
    opis: Optional[str] = None


class PenalCreate(BaseModel):
    """Model za kreiranje penala"""
    ugovor_id: int
    iznos: float
    razlog: str
    status: Literal["kreiran", "placen"] = "kreiran"


class PenalUpdate(BaseModel):
    """Model za ažuriranje penala"""
    iznos: Optional[float] = None
    razlog: Optional[str] = None
    status: Optional[Literal["kreiran", "placen"]] = None


class DnevniPrometResponse(BaseModel):
    """Response za dnevni promet"""
    datum: str
    ukupan_iznos: float


class RizicniPenalResponse(BaseModel):
    """Response za rizične penale - sa agregacijom po ugovoru"""
    timestamp: datetime
    entitet_id: int
    iznos: float
    opis: str
    ukupan_iznos_po_ugovoru: float = Field(..., description="Ukupan agregiran iznos svih penala za ovaj ugovor")
    broj_penala_po_ugovoru: int = Field(..., description="Ukupan broj penala za ovaj ugovor")


class NedeljnaAnalizaResponse(BaseModel):
    """Response za nedeljnu analizu"""
    nedelja: str
    broj_penala: int
    broj_transakcija: int

class IzvestajParametri(BaseModel):
    """Parametri za generisanje izveštaja"""
    # Prosta sekcija 1: Transakcije
    transakcije_status: Optional[Literal["na_cekanju", "uspesna", "neuspesna"]] = None
    transakcije_min_iznos: Optional[float] = None
    transakcije_max_iznos: Optional[float] = None
    
    # Prosta sekcija 2: Penali
    penali_status: Optional[Literal["kreiran", "placen"]] = None
    penali_min_iznos: Optional[float] = None
    
    # Složena sekcija: Kompleksna analiza
    dnevni_promet_days: int = Field(default=30, ge=1, le=365, description="Broj dana za dnevni promet")
    uporedna_analiza_months: int = Field(default=3, ge=1, le=12, description="Broj meseci za uporednu analizu")
    
    # Opšti parametri
    limit: int = Field(default=50, ge=1, le=500, description="Maksimalan broj zapisa u prostim sekcijama")


class IzvestajResponse(BaseModel):
    """Response nakon generisanja izveštaja"""
    message: str
    pdf_url: str
    file_name: str
    timestamp: datetime