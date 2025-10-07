from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    """Centralna konfiguracija aplikacije"""
    
    # InfluxDB
    INFLUXDB_URL: str = "http://localhost:8086"
    INFLUXDB_TOKEN: str = "my-super-secret-auth-token"
    INFLUXDB_ORG: str = "IIS_SUDPI"
    INFLUXDB_BUCKET: str = "finansijski_dogadjaji"
    
    # FastAPI
    APP_NAME: str = "Mikroservis za Analizu Finansijskih Događaja"
    APP_VERSION: str = "1.0.0"
    API_PREFIX: str = "/api"
    API_HOST: str = "0.0.0.0"
    API_PORT: int = 8001
    DEBUG: bool = True
    
    # Django API
    DJANGO_API_URL: str = "http://localhost:8000"
    
    # CORS
    CORS_ORIGINS: list = ["http://localhost:3000", "http://localhost:8000"]
    
    class Config:
        env_file = ".env"
        case_sensitive = True


settings = Settings()