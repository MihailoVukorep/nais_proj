// Brisanje postojećih podataka
//MATCH (n) DETACH DELETE n;
// 1. Kreiraj lokacije (čvorovi)
CREATE (bg:Location {name: 'Beograd', lat: 44.7866, lon: 20.4489})
CREATE (ns:Location {name: 'Novi Sad', lat: 45.2671, lon: 19.8335})
CREATE (nis:Location {name: 'Niš', lat: 43.3209, lon: 21.8958})
CREATE (kg:Location {name: 'Kragujevac', lat: 44.0128, lon: 20.9114})

// 2. Kreiraj puteve (grane sa atributima)
CREATE (bg)-[:ROAD {distanceKm: 80, durationHours: 1.5, blocked: false}]->(ns)
CREATE (bg)-[:ROAD {distanceKm: 240, durationHours: 3.0, blocked: false}]->(nis)
CREATE (ns)-[:ROAD {distanceKm: 320, durationHours: 4.0, blocked: true, reason: 'Radovi na putu'}]->(nis)
CREATE (bg)-[:ROAD {distanceKm: 140, durationHours: 2.0, blocked: false}]->(kg)

// 3. Kreiraj vozila
CREATE (v1:Vozilo {
  marka: 'Mercedes',
  model: 'Actros',
  registracija: 'BG123AB',
  kapacitetKg: 20000.0,
  status: 'slobodno'
})

CREATE (v2:Vozilo {
  marka: 'Volvo',
  model: 'FH16',
  registracija: 'NS456CD',
  kapacitetKg: 15000.0,
  status: 'slobodno'
})

CREATE (v3:Vozilo {
  marka: 'MAN',
  model: 'TGX',
  registracija: 'NI789EF',
  kapacitetKg: 18000.0,
  status: 'na_servisu'
})

// 4. Kreiraj vozače
CREATE (vo1:Vozac {
  ime: 'Marko',
  prezime: 'Marković',
  brVoznji: 15,
  status: 'slobodan'
})

CREATE (vo2:Vozac {
  ime: 'Petar',
  prezime: 'Petrović',
  brVoznji: 8,
  status: 'slobodan'
})

CREATE (vo3:Vozac {
  ime: 'Jovan',
  prezime: 'Jovanović',
  brVoznji: 22,
  status: 'na_odmoru'
})

// 5. Kreiraj rute
CREATE (r1:Route {
  startLocation: 'Beograd',
  endLocation: 'Novi Sad',
  distanceKm: 80.0,
  durationHours: 1.5,
  status: 'planirana'
})

CREATE (r2:Route {
  startLocation: 'Beograd',
  endLocation: 'Niš',
  distanceKm: 240.0,
  durationHours: 3.0,
  status: 'planirana'
})

// 6. Kreiraj isporuke sa vezama
CREATE (i1:Isporuka {
  kolicinaKg: 10000.0,
  status: 'aktivna',
  datumKreiranja: datetime('2025-12-05T08:00:00')
})

CREATE (i1)-[:USES_VEHICLE]->(v1)
CREATE (i1)-[:DRIVEN_BY]->(vo1)
CREATE (i1)-[:ON_ROUTE]->(r1)

CREATE (i2:Isporuka {
  kolicinaKg: 8000.0,
  status: 'u_toku',
  datumKreiranja: datetime('2025-12-04T10:30:00'),
  datumPolaska: datetime('2025-12-04T11:00:00')
})

CREATE (i2)-[:USES_VEHICLE]->(v2)
CREATE (i2)-[:DRIVEN_BY]->(vo2)
CREATE (i2)-[:ON_ROUTE]->(r2)

// 7. Kreiraj rampe
CREATE (rampa1:Rampa {
  oznaka: 'R01',
  status: 'slobodna'
})

CREATE (rampa2:Rampa {
  oznaka: 'R02',
  status: 'zauzeta'
})

CREATE (rampa2)-[:ASSIGNED_TO]->(i1)

// 8. Kreiraj notifikacije i upozorenja
CREATE (n1:Notifikacija {
  poruka: 'Isporuka #1 je kreirana',
  datum: datetime('2025-12-05T08:05:00'),
  procitana: false,
  korisnikEmail: 'dispecer@firma.com'
})

CREATE (u1:Upozorenje {
  tip: 'KAŠNJENJE',
  poruka: 'Isporuka #2 kasni 30 minuta',
  vreme: datetime('2025-12-04T14:30:00')
})

CREATE (u1)-[:ABOUT]->(i2)