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


class PenalCreate(BaseModel):
    """Model za kreiranje penala"""
    ugovor_id: int
    iznos: float
    razlog: str
    status: Literal["kreiran", "placen"] = "kreiran"


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