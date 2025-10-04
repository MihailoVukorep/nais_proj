from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    """Centralna konfiguracija aplikacije"""
    
    # InfluxDB
    INFLUXDB_URL: str = "http://localhost:8086"
    INFLUXDB_TOKEN: str = "moj-super-tajni-token-12345"
    INFLUXDB_ORG: str = "IIS_SUDPI"
    INFLUXDB_BUCKET: str = "finansijski_dogadjaji"
    
    # FastAPI
    APP_NAME: str = "Mikroservis za Analizu Finansijskih Događaja"
    APP_VERSION: str = "1.0.0"
    API_PREFIX: str = "/api"
    
    # CORS
    CORS_ORIGINS: list = ["http://localhost:3000", "http://localhost:8000"]
    
    class Config:
        env_file = ".env"
        case_sensitive = True


settings = Settings()