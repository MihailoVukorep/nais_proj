from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import logging
from app.config.settings import settings
from app.routes import dogadjaji, analize, izvestaji

# Logging konfiguracija
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

# Kreiranje FastAPI aplikacije
app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description="Mikroservis za analizu finansijskih događaja koristeći InfluxDB"
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
app.include_router(dogadjaji.router, prefix=settings.API_PREFIX)
app.include_router(analize.router, prefix=settings.API_PREFIX)
app.include_router(izvestaji.router, prefix=settings.API_PREFIX)


@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "message": "Mikroservis za Analizu Finansijskih Događaja",
        "version": settings.APP_VERSION,
        "docs": "/docs"
    }


@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {
        "status": "healthy",
        "service": settings.APP_NAME,
        "version": settings.APP_VERSION
    }