from dataclasses import dataclass
from typing import Optional
from datetime import datetime


@dataclass
class MerenjeResponse:
    """Response model za merenje temperature ili vlažnosti"""
    message: str
    skladiste_id: int
    senzor_id: str
    lokacija: str
    status: str


@dataclass
class ErrorResponse:
    """Response model za greške"""
    error: str
    status_code: int = 400


@dataclass
class DnevneStatistike:
    """Model za dnevne statistike"""
    datum: str
    skladiste_id: int
    prosecna_temperatura: float
    prosecna_vlaznost: float


@dataclass
class KriticniUslovi:
    """Model za kritične uslove"""
    skladiste_id: int
    kriticni_temperatura: int
    kriticni_vlaznost: int
    ukupno_kriticnih: int


@dataclass
class SenzorPerformanse:
    """Model za performanse senzora"""
    skladiste_id: int
    senzor_id: str
    lokacija: str
    broj_merenja: int
    rang: int


# Konstante za validaciju
SKLADISTE_ID_MIN = 1
SKLADISTE_ID_MAX = 5
VLAZNOST_MIN = 0
VLAZNOST_MAX = 100
TEMPERATURE_LIMIT_MIN = -50
TEMPERATURE_LIMIT_MAX = 70

# Status konstante
STATUS_OPTIMALAN = "optimalan"
STATUS_RIZIICAN = "rizičan"
STATUS_KRITIICAN = "kritičan"