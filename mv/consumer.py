#!/usr/bin/env python

import pika
import json
from neo4j import GraphDatabase

def update_supplier_reputation(event_body):
    """
    Funkcija koja izvršava drugu transakciju: ažuriranje grafa.
    """
    uri = "bolt://localhost:7687"  # Adresa vaše grafske baze
    user = "neo4j"
    password = "password"
    driver = GraphDatabase.driver(uri, auth=(user, password))

    reklamacija_id = event_body['reklamacija_id']
    dobavljac_id = event_body['dobavljac_id']
    jacina_zalbe = event_body['jacina_zalbe']

    with driver.session() as session:
        # Kreiraj čvor za reklamaciju ako ne postoji i poveži ga sa dobavljačem
        # Smanji ocenu dobavljača na osnovu jačine žalbe
        session.run("""
            MERGE (r:Reklamacija {reklamacija_id: $reklamacija_id})
            WITH r
            MATCH (d:Dobavljac {sifra_d: $dobavljac_id})
            MERGE (d)-[:IMA_REKLAMACIJU]->(r)
            SET d.ocena = d.ocena - ($jacina_zalbe * 0.1) // Primer logike za smanjenje ocene
            """, reklamacija_id=reklamacija_id, dobavljac_id=dobavljac_id, jacina_zalbe=jacina_zalbe)
    
    driver.close()
    print(f" [x] Ažurirana reputacija za dobavljača {dobavljac_id}")
