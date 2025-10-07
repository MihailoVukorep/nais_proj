from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    """Centralna konfiguracija aplikacije"""
    
    # InfluxDB
    INFLUXDB_URL: str = "http://localhost:8087"
    INFLUXDB_TOKEN: str = "warehouse-super-secret-auth-token"
    INFLUXDB_ORG: str = "IIS_SUDPI"
    INFLUXDB_BUCKET: str = "skladisni_uslovi"
    
    # FastAPI
    APP_NAME: str = "Mikroservis za Analizu Skladišnih Uslova"
    APP_VERSION: str = "1.0.0"
    API_PREFIX: str = "/api"
    API_HOST: str = "0.0.0.0"
    API_PORT: int = 8002
    DEBUG: bool = True
    
    # Django API
    DJANGO_API_URL: str = "http://localhost:8000"
    
    # CORS
    CORS_ORIGINS: list = ["http://localhost:3000", "http://localhost:8000"]
    
    # Skladišni uslovi - optimalni opsezi
    OPTIMALNA_TEMPERATURA_MIN: float = 18.0  # °C
    OPTIMALNA_TEMPERATURA_MAX: float = 24.0  # °C
    OPTIMALNA_VLAZNOST_MIN: float = 40.0     # %
    OPTIMALNA_VLAZNOST_MAX: float = 60.0     # %
    
    # Kritični uslovi
    KRITICNA_TEMPERATURA_MIN: float = 10.0   # °C
    KRITICNA_TEMPERATURA_MAX: float = 35.0   # °C
    KRITICNA_VLAZNOST_MIN: float = 20.0      # %
    KRITICNA_VLAZNOST_MAX: float = 80.0      # %
    
    class Config:
        env_file = ".env"
        case_sensitive = True


settings = Settings()