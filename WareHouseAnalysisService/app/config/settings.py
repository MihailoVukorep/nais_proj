import os
from typing import Optional

class Settings:
    """Centralizovane postavke za Flask aplikaciju"""
    
    # Flask aplikacija
    DEBUG: bool = True
    SECRET_KEY: str = os.getenv("SECRET_KEY", "dev-secret-key-change-in-production")
    HOST: str = os.getenv("HOST", "0.0.0.0")
    PORT: int = int(os.getenv("PORT", "8002"))
    
    # Swagger dokumentacija
    SWAGGER_UI_DOC_EXPANSION: str = "list"
    SWAGGER_UI_JSONEDITOR: bool = True
    SWAGGER_UI_BUNDLE_JS: str = "//unpkg.com/swagger-ui-dist@3/swagger-ui-bundle.js"
    SWAGGER_UI_STANDALONE_PRESET_JS: str = "//unpkg.com/swagger-ui-dist@3/swagger-ui-standalone-preset.js"
    SWAGGER_UI_CSS: str = "//unpkg.com/swagger-ui-dist@3/swagger-ui.css"
    
    # InfluxDB konfiguracija
    INFLUXDB_URL: str = os.getenv("INFLUXDB_URL", "http://localhost:8087")
    INFLUXDB_TOKEN: str = os.getenv("INFLUXDB_TOKEN", "warehouse-super-secret-auth-token")
    INFLUXDB_ORG: str = os.getenv("INFLUXDB_ORG", "IIS_SUDPI")
    INFLUXDB_BUCKET: str = os.getenv("INFLUXDB_BUCKET", "skladisni_uslovi")
    
    # CORS konfiguracija
    CORS_ORIGINS: list = ["http://localhost:3000", "http://localhost:8000", "*"]
    CORS_METHODS: list = ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
    CORS_HEADERS: list = ["Content-Type", "Authorization"]
    
    # Django API
    DJANGO_API_URL: str = os.getenv("DJANGO_API_URL", "http://localhost:8000")
    
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

# Globalna instanca postavki
settings = Settings()

# Helper funkcije za Docker okruženje
def is_docker_environment() -> bool:
    """Proverava da li se aplikacija izvršava u Docker kontejneru"""
    return os.path.exists('/.dockerenv') or os.getenv('CONTAINER') == 'true'

def get_influxdb_url() -> str:
    """Vraća InfluxDB URL u zavisnosti od okruženja"""
    if is_docker_environment():
        return "http://influxdb:8086"  # Docker service name
    return settings.INFLUXDB_URL

def get_app_config() -> dict:
    """Vraća Flask konfiguraciju kao dictionary"""
    return {
        'DEBUG': settings.DEBUG,
        'SECRET_KEY': settings.SECRET_KEY,
        'SWAGGER': {
            'title': 'WareHouse Analysis Service API',
            'description': 'API za praćenje i analizu skladišnih uslova',
            'version': '1.0.0',
            'doc_dir': './docs/',
            'uiversion': 3,
            'specs_route': '/apidocs/',
            'swagger_ui': True,
            'swagger_ui_bundle_js': settings.SWAGGER_UI_BUNDLE_JS,
            'swagger_ui_standalone_preset_js': settings.SWAGGER_UI_STANDALONE_PRESET_JS,
            'swagger_ui_css': settings.SWAGGER_UI_CSS,
            'swagger_ui_jsoneditor': settings.SWAGGER_UI_JSONEDITOR
        }
    }