import json
import threading
from io import BytesIO
import datetime

import pika
import requests
from flask import Flask, Response, request
from neo4j import GraphDatabase
from reportlab.lib.pagesizes import letter
from reportlab.pdfgen import canvas

# ==============================================================================
# CONFIGURATION
# Podesite ove vrednosti prema vašem okruženju
# ==============================================================================
# Adresa vaše glavne Django aplikacije
DJANGO_API_URL = "http://127.0.0.1:8000/app"
# JWT token za sigurnu komunikaciju između servisa.
# Generišite dugovečan token za namenskog "servisnog" korisnika.
DJANGO_API_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNzk5ODE2Mjk5LCJpYXQiOjE3NjgxOTUyOTksImp0aSI6Im..."

# Neo4j (Grafska baza) konekcija
NEO4J_URI = "bolt://localhost:7687"
NEO4J_USER = "neo4j"
NEO4J_PASSWORD = "your_neo4j_password" # Zamenite sa vašom lozinkom

# RabbitMQ konekcija
RABBITMQ_HOST = 'localhost'
RABBITMQ_QUEUE = 'reklamacije_queue'

# Flask aplikacija
app = Flask(__name__)

# ==============================================================================
# SAGA PATTERN - RABBITMQ CONSUMER & NEO4J UPDATE
# ==============================================================================

def update_supplier_reputation(event_body):
    """
    Izvršava transakciju u grafskoj bazi.
    Kreira čvor za reklamaciju i ažurira ocenu dobavljača.
    """
    driver = None
    try:
        driver = GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_USER, NEO4J_PASSWORD))
        with driver.session() as session:
            reklamacija_id = event_body['reklamacija_id']
            dobavljac_id = event_body['dobavljac_id']
            jacina_zalbe = event_body['jacina_zalbe']

            # Cypher upit koji atomski izvršava logiku
            # 1. Pronalazi dobavljača
            # 2. Kreira (ako ne postoji) čvor za reklamaciju
            # 3. Povezuje dobavljača i reklamaciju
            # 4. Smanjuje ocenu dobavljača na osnovu jačine žalbe
            query = """
            MATCH (d:Dobavljac {sifra_d: $dobavljac_id})
            MERGE (r:Reklamacija {reklamacija_id: $reklamacija_id})
            MERGE (d)-[:IMA_REKLAMACIJU]->(r)
            SET d.ocena = CASE
                WHEN d.ocena - ($jacina_zalbe * 0.1) < 0 THEN 0
                ELSE d.ocena - ($jacina_zalbe * 0.1)
            END
            RETURN d.naziv, d.ocena
            """
            result = session.run(query, reklamacija_id=reklamacija_id, dobavljac_id=dobavljac_id, jacina_zalbe=jacina_zalbe)
            record = result.single()
            if record:
                print(f" [x] Ažurirana ocena za dobavljača '{record['d.naziv']}' na {record['d.ocena']:.2f}")
            else:
                print(f" [!] Dobavljač sa ID {dobavljac_id} nije pronađen u graf bazi.")

    except Exception as e:
        print(f" [!] Greška pri ažuriranju Neo4j baze: {e}")
        # Ovde se može dodati logika za slanje poruke nazad u red za ponovni pokušaj
    finally:
        if driver:
            driver.close()

def start_consumer():
    """Pokreće RabbitMQ consumer-a u beskonačnoj petlji."""
    def callback(ch, method, properties, body):
        print(f" [x] Primljen događaj: {body.decode()}")
        try:
            event_body = json.loads(body)
            update_supplier_reputation(event_body)
        except json.JSONDecodeError:
            print(" [!] Greška: Primljena poruka nije validan JSON.")
        ch.basic_ack(delivery_tag=method.delivery_tag)

    connection = pika.BlockingConnection(pika.ConnectionParameters(host=RABBITMQ_HOST))
    channel = connection.channel()
    channel.queue_declare(queue=RABBITMQ_QUEUE, durable=True) # durable=True osigurava da red preživi restart
    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(queue=RABBITMQ_QUEUE, on_message_callback=callback)
    
    print(' [*] Čekam na poruke o reklamacijama. Za izlaz pritisnite CTRL+C')
    channel.start_consuming()


# ==============================================================================
# REPORT GENERATOR - FLASK ENDPOINT
# ==============================================================================

def get_data_from_django(endpoint):
    """Pomoćna funkcija za dobavljanje podataka sa Django API-ja."""
    headers = {'Authorization': f'Bearer {DJANGO_API_TOKEN}'}
    try:
        # Napomena: Django API mora biti dostupan mikroservisu na ovoj adresi
        response = requests.get(f"{DJANGO_API_URL}/{endpoint}", headers=headers)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"Greška pri komunikaciji sa Django API na '{endpoint}': {e}")
        return None

def get_alternative_suppliers_from_graph(sifra_d, ime_sirovine):
    """Kompleksni upit nad grafskom bazom."""
    driver = None
    try:
        driver = GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_USER, NEO4J_PASSWORD))
        with driver.session() as session:
            result = session.run("""
                MATCH (trenutni:Dobavljac {sifra_d: $sifra_d})
                MATCH (alternativni:Dobavljac)-[:ISPORUCUJE]->(s:Sirovina {naziv: $ime_sirovine})
                WHERE alternativni <> trenutni AND alternativni.ocena > trenutni.ocena
                RETURN alternativni.naziv AS naziv, alternativni.ocena AS ocena, alternativni.rok_isporuke AS rok_isporuke
                ORDER BY alternativni.ocena DESC LIMIT 5
                """, sifra_d=sifra_d, ime_sirovine=ime_sirovine)
            return [dict(record) for record in result]
    except Exception as e:
        print(f"Greška pri upitu u Neo4j: {e}")
        return []
    finally:
        if driver:
            driver.close()

@app.route('/reports/supplier/<int:sifra_d>', methods=['GET'])
def generate_report(sifra_d):
    # 1. Dobavi podatke iz relacione baze preko Django API-ja
    # Potrebno je da u Django app postoje endpointi koji vraćaju ove podatke
    dobavljac = get_data_from_django(f"suppliers/{sifra_d}/")
    # Pretpostavka je da Django API podržava filtriranje, npr. /visits/?dobavljac_id=...
    posete = get_data_from_django(f"visits/?dobavljac_id={sifra_d}") or []
    reklamacije = get_data_from_django(f"complaints/?dobavljac_id={sifra_d}") or []

    if not dobavljac:
        return "Dobavljač nije pronađen ili greška u komunikaciji sa glavnim servisom.", 404

    # 2. Dobavi podatke iz grafske baze
    alternatives = get_alternative_suppliers_from_graph(sifra_d, dobavljac.get('ime_sirovine'))

    # 3. Generiši PDF u memoriji
    buffer = BytesIO()
    p = canvas.Canvas(buffer, pagesize=letter)
    width, height = letter
    y_position = height - 50

    # Zaglavlje
    p.setFont("Helvetica-Bold", 16)
    p.drawString(100, y_position, f"Analiza kvaliteta: {dobavljac.get('naziv')}")
    y_position -= 20
    p.setFont("Helvetica", 10)
    p.drawString(100, y_position, f"PIB: {dobavljac.get('PIB_d')} | Email: {dobavljac.get('email')}")
    y_position -= 15
    p.drawString(100, y_position, f"Datum izveštaja: {datetime.date.today().strftime('%d.%m.%Y.')}")
    y_position -= 30

    # Prosta sekcija 1: Posete
    p.setFont("Helvetica-Bold", 12)
    p.drawString(100, y_position, "Istorija poseta (iz relacione baze)")
    y_position -= 20
    p.setFont("Helvetica", 10)
    if posete:
        for poseta in posete[:5]: # Prikazujemo najviše 5
            p.drawString(120, y_position, f"- {poseta.get('datum_od')}, Status: {poseta.get('status')}")
            y_position -= 15
    else:
        p.drawString(120, y_position, "Nema zabeleženih poseta.")
        y_position -= 15
    
    y_position -= 20

    # Prosta sekcija 2: Reklamacije
    p.setFont("Helvetica-Bold", 12)
    p.drawString(100, y_position, "Istorija reklamacija (iz relacione baze)")
    y_position -= 20
    p.setFont("Helvetica", 10)
    if reklamacije:
        for r in reklamacije[:5]:
            p.drawString(120, y_position, f"- {r.get('datum_prijema')}, Jačina: {r.get('jacina_zalbe')}/10, Opis: {r.get('opis_problema', '')[:40]}...")
            y_position -= 15
    else:
        p.drawString(120, y_position, "Nema zabeleženih reklamacija.")
        y_position -= 15

    y_position -= 20

    # Složena sekcija: Alternativni dobavljači
    p.setFont("Helvetica-Bold", 12)
    p.drawString(100, y_position, f"Preporuka alternativa za sirovinu '{dobavljac.get('ime_sirovine')}' (iz grafske baze)")
    y_position -= 20
    p.setFont("Helvetica", 10)
    if alternatives:
        for alt in alternatives:
            p.drawString(120, y_position, f"- {alt['naziv']} (Ocena: {alt['ocena']:.2f}, Rok isporuke: {alt['rok_isporuke']} dana)")
            y_position -= 15
    else:
        p.drawString(120, y_position, "Nema preporučenih alternativnih dobavljača sa boljom ocenom.")

    p.showPage()
    p.save()
    
    buffer.seek(0)
    return Response(buffer, mimetype='application/pdf', headers={
        'Content-Disposition': f'attachment;filename=izvestaj_{dobavljac.get("naziv")}.pdf'
    })

# ==============================================================================
# POKRETANJE SERVISA
# ==============================================================================

if __name__ == '__main__':
    # Pokretanje RabbitMQ consumera u pozadinskoj niti (thread)
    consumer_thread = threading.Thread(target=start_consumer)
    consumer_thread.daemon = True  # Omogućava da se nit ugasi kad se glavni program završi
    consumer_thread.start()

    # Pokretanje Flask web servera
    # U produkciji koristiti Gunicorn ili sličan WSGI server
    app.run(host='0.0.0.0', port=5001, debug=False)
