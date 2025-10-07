from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import logging
from app.config.settings import settings
from app.routes import merenja

# Logging konfiguracija
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

# Kreiranje FastAPI aplikacije
app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description="Mikroservis za analizu skladišnih uslova koristeći InfluxDB"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Registracija rutera
app.include_router(merenja.router, prefix=settings.API_PREFIX)  # Jedina potrebna ruta


@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "message": "Mikroservis za Analizu Skladišnih Uslova",
        "version": settings.APP_VERSION,
        "docs": "/docs",
        "optimalni_uslovi": {
            "temperatura": f"{settings.OPTIMALNA_TEMPERATURA_MIN}-{settings.OPTIMALNA_TEMPERATURA_MAX}°C",
            "vlaznost": f"{settings.OPTIMALNA_VLAZNOST_MIN}-{settings.OPTIMALNA_VLAZNOST_MAX}%"
        }
    }


@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {
        "status": "healthy",
        "service": settings.APP_NAME,
        "version": settings.APP_VERSION,
        "influxdb_url": settings.INFLUXDB_URL
    }