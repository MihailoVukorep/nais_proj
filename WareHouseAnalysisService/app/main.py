from flask import Flask, jsonify
from flask_cors import CORS
from flasgger import Swagger
import logging
from app.config.settings import settings, get_app_config
from app.routes import merenja

# Logging konfiguracija
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

# Kreiranje Flask aplikacije
app = Flask(__name__)

# Učitavanje konfiguracije
app.config.update(get_app_config())

# CORS setup
CORS(app, 
     origins=settings.CORS_ORIGINS, 
     methods=settings.CORS_METHODS,
     allow_headers=settings.CORS_HEADERS,
     supports_credentials=True)

# Swagger setup
swagger = Swagger(app)

# Registracija blueprint-a
app.register_blueprint(merenja.bp, url_prefix='/api')


@app.route("/")
def root():
    """Root endpoint
    ---
    responses:
      200:
        description: Osnovne informacije o servisu
        schema:
          type: object
          properties:
            message:
              type: string
            docs:
              type: string
            optimalni_uslovi:
              type: object
    """
    return jsonify({
        "message": "Mikroservis za Analizu Skladišnih Uslova",
        "docs": "/apidocs/",
        "optimalni_uslovi": {
            "temperatura": f"{settings.OPTIMALNA_TEMPERATURA_MIN}-{settings.OPTIMALNA_TEMPERATURA_MAX}°C",
            "vlaznost": f"{settings.OPTIMALNA_VLAZNOST_MIN}-{settings.OPTIMALNA_VLAZNOST_MAX}%"
        }
    })


@app.route("/health")
def health_check():
    """Health check endpoint
    ---
    responses:
      200:
        description: Status servisa
        schema:
          type: object
          properties:
            status:
              type: string
            service:
              type: string
            influxdb_url:
              type: string
    """
    return jsonify({
        "status": "healthy",
        "service": "WareHouse Analysis Service",
        "influxdb_url": settings.INFLUXDB_URL
    })


if __name__ == '__main__':
    app.run(host=settings.HOST, port=settings.PORT, debug=settings.DEBUG)