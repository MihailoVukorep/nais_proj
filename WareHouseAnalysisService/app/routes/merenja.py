from flask import Blueprint, request, jsonify
from flasgger import swag_from
from datetime import datetime
from app.services.influx_service import influx_service
import logging

bp = Blueprint('merenja', __name__)
logger = logging.getLogger(__name__)


# ===== TEMPERATURA MERENJA =====

@bp.route("/merenja/temperatura", methods=['POST'])
@swag_from({
    'tags': ['Merenja Temperature i Vlažnosti'],
    'summary': 'Kreira novo merenje temperature',
    'parameters': [
        {
            'name': 'skladiste_id',
            'in': 'query',
            'type': 'integer',
            'required': True,
            'minimum': 1,
            'maximum': 5,
            'description': 'ID skladišta (1-5)'
        },
        {
            'name': 'temperatura',
            'in': 'query',
            'type': 'number',
            'required': True,
            'description': 'Temperatura u °C'
        },
        {
            'name': 'senzor_id',
            'in': 'query',
            'type': 'string',
            'required': True,
            'description': 'ID senzora'
        },
        {
            'name': 'lokacija',
            'in': 'query',
            'type': 'string',
            'required': True,
            'description': 'Lokacija u skladištu'
        }
    ],
    'responses': {
        201: {
            'description': 'Merenje temperature uspešno kreirano',
            'schema': {
                'type': 'object',
                'properties': {
                    'message': {'type': 'string'},
                    'skladiste_id': {'type': 'integer'},
                    'temperatura': {'type': 'number'},
                    'senzor_id': {'type': 'string'},
                    'status': {'type': 'string'}
                }
            }
        }
    }
})
def kreiraj_merenje_temperature():
    """CREATE - Kreira novo merenje temperature"""
    try:
        skladiste_id = int(request.args.get('skladiste_id'))
        temperatura = float(request.args.get('temperatura'))
        senzor_id = request.args.get('senzor_id')
        lokacija = request.args.get('lokacija')
        
        if not all([skladiste_id, temperatura is not None, senzor_id, lokacija]):
            return jsonify({"error": "Nedostaju obavezni parametri"}), 400
        
        if not 1 <= skladiste_id <= 5:
            return jsonify({"error": "skladiste_id mora biti između 1 i 5"}), 400
        
        influx_service.write_merenje_temperature(
            skladiste_id=skladiste_id,
            temperatura=temperatura,
            senzor_id=senzor_id,
            lokacija=lokacija
        )
        
        return jsonify({
            "message": "Merenje temperature uspešno kreirano",
            "skladiste_id": skladiste_id,
            "temperatura": temperatura,
            "senzor_id": senzor_id,
            "status": influx_service._determine_temperature_status(temperatura)
        }), 201
        
    except Exception as e:
        logger.error(f"Greška pri kreiranju merenja temperature: {str(e)}")
        return jsonify({"error": str(e)}), 500


@bp.route("/merenja/temperatura", methods=['GET'])
@swag_from({
    'tags': ['Merenja Temperature i Vlažnosti'],
    'summary': 'Vraća merenja temperature',
    'parameters': [
        {
            'name': 'skladiste_id',
            'in': 'query',
            'type': 'integer',
            'required': False,
            'minimum': 1,
            'maximum': 5,
            'description': 'ID skladišta (1-5)'
        },
        {
            'name': 'limit',
            'in': 'query',
            'type': 'integer',
            'required': False,
            'default': 100,
            'minimum': 1,
            'maximum': 1000,
            'description': 'Maksimalan broj rezultata'
        }
    ],
    'responses': {
        200: {
            'description': 'Lista merenja temperature',
            'schema': {
                'type': 'array',
                'items': {
                    'type': 'object',
                    'properties': {
                        'timestamp': {'type': 'string'},
                        'skladiste_id': {'type': 'integer'},
                        'temperatura': {'type': 'number'},
                        'senzor_id': {'type': 'string'},
                        'lokacija': {'type': 'string'},
                        'status': {'type': 'string'}
                    }
                }
            }
        }
    }
})
def dohvati_merenja_temperature():
    """READ - Vraća merenja temperature"""
    try:
        skladiste_id = request.args.get('skladiste_id', type=int)
        limit = request.args.get('limit', default=100, type=int)
        
        if skladiste_id and not 1 <= skladiste_id <= 5:
            return jsonify({"error": "skladiste_id mora biti između 1 i 5"}), 400
        
        if not 1 <= limit <= 1000:
            return jsonify({"error": "limit mora biti između 1 i 1000"}), 400
        
        skladiste_filter = f'|> filter(fn: (r) => r.skladiste_id == "{skladiste_id}")' if skladiste_id else ""
        
        flux_query = f'''
from(bucket: "{influx_service.bucket}")
  |> range(start: -30d)
  |> filter(fn: (r) => r._measurement == "merenja_temperatura")
  |> filter(fn: (r) => r._field == "vrednost")
  {skladiste_filter}
  |> sort(columns: ["_time"], desc: true)
  |> limit(n: {limit})
  |> yield(name: "temperatura_merenja")
        '''
        
        result = influx_service.query_api.query(flux_query, org=influx_service.org)
        data = []
        
        for table in result:
            for record in table.records:
                data.append({
                    "timestamp": record.get_time().isoformat(),
                    "skladiste_id": int(record.values.get('skladiste_id')),
                    "temperatura": record.get_value(),
                    "senzor_id": record.values.get('senzor_id'),
                    "lokacija": record.values.get('lokacija'),
                    "status": record.values.get('status')
                })
        
        return jsonify(data)
        
    except Exception as e:
        logger.error(f"Greška pri dohvatanju merenja temperature: {str(e)}")
        return jsonify({"error": str(e)}), 500


@bp.route("/merenja/temperatura/<int:skladiste_id>/<senzor_id>", methods=['DELETE'])
@swag_from({
    'tags': ['Merenja Temperature i Vlažnosti'],
    'summary': 'Briše merenje temperature',
    'parameters': [
        {
            'name': 'skladiste_id',
            'in': 'path',
            'type': 'integer',
            'required': True,
            'description': 'ID skladišta'
        },
        {
            'name': 'senzor_id',
            'in': 'path',
            'type': 'string',
            'required': True,
            'description': 'ID senzora'
        },
        {
            'name': 'timestamp',
            'in': 'query',
            'type': 'string',
            'required': True,
            'description': 'Timestamp merenja za brisanje (ISO format)'
        }
    ],
    'responses': {
        200: {
            'description': 'Merenje temperature uspešno obrisano',
            'schema': {
                'type': 'object',
                'properties': {
                    'message': {'type': 'string'},
                    'skladiste_id': {'type': 'integer'},
                    'senzor_id': {'type': 'string'},
                    'timestamp': {'type': 'string'}
                }
            }
        }
    }
})
def obrisi_merenje_temperature(skladiste_id, senzor_id):
    """DELETE - Briše merenje temperature"""
    try:
        timestamp_str = request.args.get('timestamp')
        if not timestamp_str:
            return jsonify({"error": "Timestamp parametar je obavezan"}), 400
        
        timestamp_dt = datetime.fromisoformat(timestamp_str.replace('Z', '+00:00'))
        
        success = influx_service.delete_merenje_temperature(skladiste_id, senzor_id, timestamp_dt)
        
        if not success:
            return jsonify({"error": "Merenje nije pronađeno"}), 404
        
        return jsonify({
            "message": "Merenje temperature uspešno obrisano",
            "skladiste_id": skladiste_id,
            "senzor_id": senzor_id,
            "timestamp": timestamp_str
        })
        
    except Exception as e:
        logger.error(f"Greška pri brisanju merenja temperature: {str(e)}")
        return jsonify({"error": str(e)}), 500


# ===== VLAŽNOST MERENJA =====

@bp.route("/merenja/vlaznost", methods=['POST'])
@swag_from({
    'tags': ['Merenja Temperature i Vlažnosti'],
    'summary': 'Kreira novo merenje vlažnosti',
    'parameters': [
        {
            'name': 'skladiste_id',
            'in': 'query',
            'type': 'integer',
            'required': True,
            'minimum': 1,
            'maximum': 5,
            'description': 'ID skladišta (1-5)'
        },
        {
            'name': 'vlaznost',
            'in': 'query',
            'type': 'number',
            'required': True,
            'minimum': 0,
            'maximum': 100,
            'description': 'Vlažnost u %'
        },
        {
            'name': 'senzor_id',
            'in': 'query',
            'type': 'string',
            'required': True,
            'description': 'ID senzora'
        },
        {
            'name': 'lokacija',
            'in': 'query',
            'type': 'string',
            'required': True,
            'description': 'Lokacija u skladištu'
        }
    ],
    'responses': {
        201: {
            'description': 'Merenje vlažnosti uspešno kreirano',
            'schema': {
                'type': 'object',
                'properties': {
                    'message': {'type': 'string'},
                    'skladiste_id': {'type': 'integer'},
                    'vlaznost': {'type': 'number'},
                    'senzor_id': {'type': 'string'},
                    'status': {'type': 'string'}
                }
            }
        }
    }
})
def kreiraj_merenje_vlaznosti():
    """CREATE - Kreira novo merenje vlažnosti"""
    try:
        skladiste_id = int(request.args.get('skladiste_id'))
        vlaznost = float(request.args.get('vlaznost'))
        senzor_id = request.args.get('senzor_id')
        lokacija = request.args.get('lokacija')
        
        if not all([skladiste_id, vlaznost is not None, senzor_id, lokacija]):
            return jsonify({"error": "Nedostaju obavezni parametri"}), 400
        
        if not 1 <= skladiste_id <= 5:
            return jsonify({"error": "skladiste_id mora biti između 1 i 5"}), 400
        
        if not 0 <= vlaznost <= 100:
            return jsonify({"error": "vlaznost mora biti između 0 i 100"}), 400
        
        influx_service.write_merenje_vlaznost(
            skladiste_id=skladiste_id,
            vlaznost=vlaznost,
            senzor_id=senzor_id,
            lokacija=lokacija
        )
        
        return jsonify({
            "message": "Merenje vlažnosti uspešno kreirano",
            "skladiste_id": skladiste_id,
            "vlaznost": vlaznost,
            "senzor_id": senzor_id,
            "status": influx_service._determine_humidity_status(vlaznost)
        }), 201
        
    except Exception as e:
        logger.error(f"Greška pri kreiranju merenja vlažnosti: {str(e)}")
        return jsonify({"error": str(e)}), 500


@bp.route("/merenja/vlaznost", methods=['GET'])
@swag_from({
    'tags': ['Merenja Temperature i Vlažnosti'],
    'summary': 'Vraća merenja vlažnosti',
    'parameters': [
        {
            'name': 'skladiste_id',
            'in': 'query',
            'type': 'integer',
            'required': False,
            'minimum': 1,
            'maximum': 5,
            'description': 'ID skladišta (1-5)'
        },
        {
            'name': 'limit',
            'in': 'query',
            'type': 'integer',
            'required': False,
            'default': 100,
            'minimum': 1,
            'maximum': 1000,
            'description': 'Maksimalan broj rezultata'
        }
    ],
    'responses': {
        200: {
            'description': 'Lista merenja vlažnosti',
            'schema': {
                'type': 'array',
                'items': {
                    'type': 'object',
                    'properties': {
                        'timestamp': {'type': 'string'},
                        'skladiste_id': {'type': 'integer'},
                        'vlaznost': {'type': 'number'},
                        'senzor_id': {'type': 'string'},
                        'lokacija': {'type': 'string'},
                        'status': {'type': 'string'}
                    }
                }
            }
        }
    }
})
def dohvati_merenja_vlaznosti():
    """READ - Vraća merenja vlažnosti"""
    try:
        skladiste_id = request.args.get('skladiste_id', type=int)
        limit = request.args.get('limit', default=100, type=int)
        
        if skladiste_id and not 1 <= skladiste_id <= 5:
            return jsonify({"error": "skladiste_id mora biti između 1 i 5"}), 400
        
        if not 1 <= limit <= 1000:
            return jsonify({"error": "limit mora biti između 1 i 1000"}), 400
        
        skladiste_filter = f'|> filter(fn: (r) => r.skladiste_id == "{skladiste_id}")' if skladiste_id else ""
        
        flux_query = f'''
from(bucket: "{influx_service.bucket}")
  |> range(start: -30d)
  |> filter(fn: (r) => r._measurement == "merenja_vlaznost")
  |> filter(fn: (r) => r._field == "vrednost")
  {skladiste_filter}
  |> sort(columns: ["_time"], desc: true)
  |> limit(n: {limit})
  |> yield(name: "vlaznost_merenja")
        '''
        
        result = influx_service.query_api.query(flux_query, org=influx_service.org)
        data = []
        
        for table in result:
            for record in table.records:
                data.append({
                    "timestamp": record.get_time().isoformat(),
                    "skladiste_id": int(record.values.get('skladiste_id')),
                    "vlaznost": record.get_value(),
                    "senzor_id": record.values.get('senzor_id'),
                    "lokacija": record.values.get('lokacija'),
                    "status": record.values.get('status')
                })
        
        return jsonify(data)
        
    except Exception as e:
        logger.error(f"Greška pri dohvatanju merenja vlažnosti: {str(e)}")
        return jsonify({"error": str(e)}), 500


@bp.route("/merenja/vlaznost/<int:skladiste_id>/<senzor_id>", methods=['DELETE'])
@swag_from({
    'tags': ['Merenja Temperature i Vlažnosti'],
    'summary': 'Briše merenje vlažnosti',
    'parameters': [
        {
            'name': 'skladiste_id',
            'in': 'path',
            'type': 'integer',
            'required': True,
            'description': 'ID skladišta'
        },
        {
            'name': 'senzor_id',
            'in': 'path',
            'type': 'string',
            'required': True,
            'description': 'ID senzora'
        },
        {
            'name': 'timestamp',
            'in': 'query',
            'type': 'string',
            'required': True,
            'description': 'Timestamp merenja za brisanje (ISO format)'
        }
    ],
    'responses': {
        200: {
            'description': 'Merenje vlažnosti uspešno obrisano',
            'schema': {
                'type': 'object',
                'properties': {
                    'message': {'type': 'string'},
                    'skladiste_id': {'type': 'integer'},
                    'senzor_id': {'type': 'string'},
                    'timestamp': {'type': 'string'}
                }
            }
        }
    }
})
def obrisi_merenje_vlaznosti(skladiste_id, senzor_id):
    """DELETE - Briše merenje vlažnosti"""
    try:
        timestamp_str = request.args.get('timestamp')
        if not timestamp_str:
            return jsonify({"error": "Timestamp parametar je obavezan"}), 400
        
        timestamp_dt = datetime.fromisoformat(timestamp_str.replace('Z', '+00:00'))
        
        success = influx_service.delete_merenje_vlaznost(skladiste_id, senzor_id, timestamp_dt)
        
        if not success:
            return jsonify({"error": "Merenje nije pronađeno"}), 404
        
        return jsonify({
            "message": "Merenje vlažnosti uspešno obrisano",
            "skladiste_id": skladiste_id,
            "senzor_id": senzor_id,
            "timestamp": timestamp_str
        })
        
    except Exception as e:
        logger.error(f"Greška pri brisanju merenja vlažnosti: {str(e)}")
        return jsonify({"error": str(e)}), 500


# ===== SLOŽENI UPITI =====

@bp.route("/merenja/analize/dnevne-statistike", methods=['GET'])
@swag_from({
    'tags': ['Merenja Temperature i Vlažnosti'],
    'summary': 'Dnevne statistike po skladištu',
    'description': 'SLOŽEN UPIT 1: Kombinuje filtriranje + grupisanje + agregaciju + sortiranje',
    'parameters': [
        {
            'name': 'days',
            'in': 'query',
            'type': 'integer',
            'required': False,
            'default': 30,
            'minimum': 1,
            'maximum': 365,
            'description': 'Broj dana unazad'
        }
    ],
    'responses': {
        200: {
            'description': 'Dnevne statistike',
            'schema': {
                'type': 'array',
                'items': {
                    'type': 'object',
                    'properties': {
                        'datum': {'type': 'string'},
                        'skladiste_id': {'type': 'integer'},
                        'prosecna_temperatura': {'type': 'number'},
                        'prosecna_vlaznost': {'type': 'number'}
                    }
                }
            }
        }
    }
})
def dnevne_statistike_po_skladistu():
    """SLOŽEN UPIT 1: Dnevne statistike po skladištu"""
    try:
        days = request.args.get('days', default=30, type=int)
        if not 1 <= days <= 365:
            return jsonify({"error": "days mora biti između 1 i 365"}), 400
        
        rezultat = influx_service.query_complex_1_daily_stats_by_warehouse(days)
        return jsonify(rezultat)
        
    except Exception as e:
        logger.error(f"Greška pri dnevnim statistikama: {str(e)}")
        return jsonify({"error": str(e)}), 500


@bp.route("/merenja/analize/kriticni-uslovi-agregacija", methods=['GET'])
@swag_from({
    'tags': ['Merenja Temperature i Vlažnosti'],
    'summary': 'Agregacija kritičnih uslova po skladištu',
    'description': 'SLOŽEN UPIT 2: Kombinuje filtriranje + grupisanje + agregaciju + sortiranje',
    'parameters': [
        {
            'name': 'days',
            'in': 'query',
            'type': 'integer',
            'required': False,
            'default': 365,
            'minimum': 1,
            'maximum': 365,
            'description': 'Broj dana unazad'
        }
    ],
    'responses': {
        200: {
            'description': 'Agregacija kritičnih uslova',
            'schema': {
                'type': 'array',
                'items': {
                    'type': 'object',
                    'properties': {
                        'skladiste_id': {'type': 'integer'},
                        'kriticni_temperatura': {'type': 'integer'},
                        'kriticni_vlaznost': {'type': 'integer'},
                        'ukupno_kriticnih': {'type': 'integer'}
                    }
                }
            }
        }
    }
})
def kriticni_uslovi_agregacija():
    """SLOŽEN UPIT 2: Agregacija kritičnih uslova po skladištu"""
    try:
        days = request.args.get('days', default=7, type=int)
        if not 1 <= days <= 365:
            return jsonify({"error": "days mora biti između 1 i 365"}), 400
        
        rezultat = influx_service.query_complex_2_critical_conditions_aggregated(days)
        return jsonify(rezultat)
        
    except Exception as e:
        logger.error(f"Greška pri agregaciji kritičnih uslova: {str(e)}")
        return jsonify({"error": str(e)}), 500


@bp.route("/merenja/analize/senzori-ranking", methods=['GET'])
@swag_from({
    'tags': ['Merenja Temperature i Vlažnosti'],
    'summary': 'Ranking performansi senzora',
    'description': 'SLOŽEN UPIT 3: Kombinuje filtriranje + grupisanje + agregaciju + sortiranje',
    'parameters': [
        {
            'name': 'days',
            'in': 'query',
            'type': 'integer',
            'required': False,
            'default': 14,
            'minimum': 1,
            'maximum': 90,
            'description': 'Broj dana unazad'
        }
    ],
    'responses': {
        200: {
            'description': 'Ranking performansi senzora',
            'schema': {
                'type': 'array',
                'items': {
                    'type': 'object',
                    'properties': {
                        'skladiste_id': {'type': 'integer'},
                        'senzor_id': {'type': 'string'},
                        'lokacija': {'type': 'string'},
                        'broj_merenja': {'type': 'integer'},
                        'rang': {'type': 'integer'}
                    }
                }
            }
        }
    }
})
def ranking_performansi_senzora():
    """SLOŽEN UPIT 3: Ranking performansi senzora"""
    try:
        days = request.args.get('days', default=14, type=int)
        if not 1 <= days <= 90:
            return jsonify({"error": "days mora biti između 1 i 90"}), 400
        
        rezultat = influx_service.query_complex_3_sensor_performance_ranking(days)
        return jsonify(rezultat)
        
    except Exception as e:
        logger.error(f"Greška pri ranking senzora: {str(e)}")
        return jsonify({"error": str(e)}), 500