from pydantic import BaseModel, Field
from typing import Literal, Optional
from datetime import datetime


class KlimatskiUslovi(BaseModel):
    """Osnovni model za klimatske uslove u skladištu"""
    skladiste_id: int = Field(..., description="ID skladišta")
    temperatura: float = Field(..., description="Temperatura u Celsius stepenim")
    vlaznost: float = Field(..., description="Vlažnost u procentima")
    senzor_id: str = Field(..., description="ID senzora koji je zabeležio podatak")
    lokacija: str = Field(..., description="Lokacija u skladištu (zona, red, polica)")


class KlimatskiUsloviCreate(KlimatskiUslovi):
    """Model za kreiranje novog merenja klimatskih uslova"""
    pass


class KlimatskiUsloviResponse(KlimatskiUslovi):
    """Model za odgovor sa klimatskim uslovima"""
    timestamp: datetime = Field(..., description="Vreme merenja")
    status_temperatura: Literal["optimalna", "rizična", "kritična"] = Field(..., description="Status temperature")
    status_vlaznost: Literal["optimalna", "rizična", "kritična"] = Field(..., description="Status vlažnosti")
    ukupni_status: Literal["optimalan", "rizičan", "kritičan"] = Field(..., description="Ukupni status uslova")
    
    class Config:
        from_attributes = True


class TemperaturaUpdate(BaseModel):
    """Model za ažuriranje temperature"""
    temperatura: float = Field(..., description="Nova vrednost temperature")
    senzor_id: Optional[str] = None
    lokacija: Optional[str] = None


class VlaznostUpdate(BaseModel):
    """Model za ažuriranje vlažnosti"""
    vlaznost: float = Field(..., description="Nova vrednost vlažnosti")
    senzor_id: Optional[str] = None
    lokacija: Optional[str] = None


class KlimatskiUsloviUpdate(BaseModel):
    """Model za ažuriranje kompletnih klimatskih uslova"""
    temperatura: Optional[float] = None
    vlaznost: Optional[float] = None
    senzor_id: Optional[str] = None
    lokacija: Optional[str] = None


class DnevniUsloviResponse(BaseModel):
    """Response za dnevne uslove"""
    datum: str
    skladiste_id: int
    prosecna_temperatura: float
    prosecna_vlaznost: float
    min_temperatura: float
    max_temperatura: float
    min_vlaznost: float
    max_vlaznost: float
    broj_merenja: int


class KriticniUsloviResponse(BaseModel):
    """Response za kritične uslove"""
    timestamp: datetime
    skladiste_id: int
    temperatura: float
    vlaznost: float
    senzor_id: str
    lokacija: str
    tip_problema: Literal["kritična_temperatura", "kritična_vlažnost", "oba_kritična"]
    opis_problema: str


class NedeljnaAnalizaResponse(BaseModel):
    """Response za nedeljnu analizu"""
    nedelja: str
    skladiste_id: int
    broj_optimalnih: int
    broj_rizicnih: int
    broj_kriticnih: int
    prosecna_temperatura: float
    prosecna_vlaznost: float


class TrendAnalizaResponse(BaseModel):
    """Response za analizu trendova"""
    skladiste_id: int
    period: str
    trend_temperature: Literal["raste", "opada", "stabilan"]
    trend_vlaznosti: Literal["raste", "opada", "stabilan"]
    korelacija: float = Field(..., description="Korelacija između temperature i vlažnosti")


# ===== MODELI ZA IZVEŠTAJE =====

class IzvestajParametri(BaseModel):
    """Parametri za generisanje izveštaja"""
    # Skladište filter
    skladiste_id: Optional[int] = None
    
    # Prosta sekcija 1: Trenutni uslovi
    trenutni_uslovi_limit: int = Field(default=50, ge=1, le=200, description="Broj najnovijih merenja")
    
    # Prosta sekcija 2: Kritični uslovi
    kriticni_uslovi_days: int = Field(default=7, ge=1, le=30, description="Broj dana za analizu kritičnih uslova")
    
    # Složena sekcija: Kompleksna analiza
    dnevni_uslovi_days: int = Field(default=30, ge=1, le=365, description="Broj dana za dnevne uslove")
    trend_analiza_days: int = Field(default=14, ge=7, le=90, description="Broj dana za trend analizu")
    
    # Opšti parametri
    limit: int = Field(default=100, ge=1, le=500, description="Maksimalan broj zapisa u sekcijama")


class IzvestajResponse(BaseModel):
    """Response nakon generisanja izveštaja"""
    message: str
    pdf_url: str
    file_name: str
    timestamp: datetime
    skladiste_id: Optional[int] = None


class SkladisteInfo(BaseModel):
    """Informacije o skladištu"""
    skladiste_id: int
    naziv: str
    tip_robe: str = Field(..., description="Tip robe koji se čuva")
    kapacitet: float = Field(..., description="Kapacitet skladišta u m³")
    
    
class StatistikeResponse(BaseModel):
    """Response sa statistikama skladišta"""
    skladiste_id: int
    ukupno_merenja: int
    poslednje_merenje: Optional[datetime]
    prosecna_temperatura: float
    prosecna_vlaznost: float
    procenat_optimalnih_uslova: float
    procenat_kriticnih_uslova: float