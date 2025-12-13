## Opis projekta

Projekat predstavlja backend sistem za upravljanje logističkim procesima koji uključuju isporuke, vozila, vozače i rute. Sistem koristi grafovsku bazu podataka (**Neo4j**) za modelovanje relacija između entiteta, kao i relacionu bazu podataka za dodatne poslovne transakcije. Aplikacija je razvijena korišćenjem **Spring Boot** okruženja.

---

## Korišćene tehnologije

- Java  
- Spring Boot  
- Spring Data Neo4j  
- Neo4j (grafovska baza podataka)  
- REST API  
- Maven  

---

## Funkcionalnosti

### CRUD operacije za grafove i relacije
- Upravljanje čvorovima: **Isporuka**, **Vozilo**, **Vozac**, **Route**
- Upravljanje relacijama između entiteta

### Kompleksni upiti nad grafovskom bazom
- Promena statusa puta u "blokiran", prilikom odstupanja ili bilo kog vida kašnjenja isporuke i navodjenje razloga zbog čega je do toga došlo
- Evidencija blokiranih puteva i razloga zbog čega je do toga došlo
- Promena statusa rampe i isporuke prilikom utovara
- Statistika kapaciteta voznog parka 
- Rangiranje i preporuka vozača na osnovu broja obavljenih voznji, isporuka i ukupnog kapaciteta isporuka

### Transakcije između relacionih i grafovskih baza
- Sinhronizacija podataka između više servisa  
- Obrada grešaka i očuvanje konzistentnosti podataka
- Promena statusa vozila - Orchestration-based Saga

### Generisanje izveštaja
- Generisanje PDF izveštaja na osnovu podataka iz baze  

---

## Struktura izveštaja

Izveštaj je organizovan u tri sekcije:

1. Prikaz razloga kašnjenja isporuka i rute na kojoj se to dogodilo 
2. Prikaz vozila koji su u kvaru ili na servisu, na osnovu potrebnog kapaciteta usled nedovoljnog broja vozila
3. Analitička sekcija sa prikazom vozača sa najvišim zaslugama
