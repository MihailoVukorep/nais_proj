// 1. Brisanje postojećih podataka
MATCH (n) DETACH DELETE n;

// 2. Kreiraj lokacije (gradovi i mesta u Srbiji) - UPROŠĆENO BEZ 'type'
CREATE (bg:Location {name: 'Beograd', lat: 44.7866, lon: 20.4489})
CREATE (ns:Location {name: 'Novi Sad', lat: 45.2671, lon: 19.8335})
CREATE (nis:Location {name: 'Niš', lat: 43.3209, lon: 21.8958})
CREATE (kg:Location {name: 'Kragujevac', lat: 44.0128, lon: 20.9114})
CREATE (sub:Location {name: 'Subotica', lat: 46.1005, lon: 19.6655})
CREATE (zr:Location {name: 'Zrenjanin', lat: 45.3836, lon: 20.3819})
CREATE (som:Location {name: 'Sombor', lat: 45.7731, lon: 19.1142})
CREATE (pancevo:Location {name: 'Pančevo', lat: 44.874, lon: 20.647})
CREATE (sm:Location {name: 'Smederevo', lat: 44.6659, lon: 20.9396})
CREATE (kraljevo:Location {name: 'Kraljevo', lat: 43.7259, lon: 20.6896})
CREATE (leskovac:Location {name: 'Leskovac', lat: 42.9976, lon: 21.9445})
CREATE (vranje:Location {name: 'Vranje', lat: 42.5524, lon: 21.9003})
CREATE (uzice:Location {name: 'Užice', lat: 43.8556, lon: 19.8425})
CREATE (cacak:Location {name: 'Čačak', lat: 43.8914, lon: 20.3497})
CREATE (valjevo:Location {name: 'Valjevo', lat: 44.2725, lon: 19.8875})
CREATE (sabac:Location {name: 'Šabac', lat: 44.7566, lon: 19.6909})
CREATE (pozega:Location {name: 'Požega', lat: 43.8456, lon: 20.0369})
CREATE (krusevac:Location {name: 'Kruševac', lat: 43.5800, lon: 21.3339})
CREATE (paracin:Location {name: 'Paraćin', lat: 43.8608, lon: 21.4117})
CREATE (smedpalanka:Location {name: 'Smederevska Palanka', lat: 44.3667, lon: 20.9667})
CREATE (indjija:Location {name: 'Indjija', lat: 45.0481, lon: 20.0894})
CREATE (ruma:Location {name: 'Ruma', lat: 45.0081, lon: 19.8222})
CREATE (stpazova:Location {name: 'Stara Pazova', lat: 44.9850, lon: 20.1603})
CREATE (becej:Location {name: 'Bečej', lat: 45.6167, lon: 20.0333})
CREATE (vrsac:Location {name: 'Vršac', lat: 45.1236, lon: 21.2983})
CREATE (kikinda:Location {name: 'Kikinda', lat: 45.8289, lon: 20.4653})
CREATE (senta:Location {name: 'Senta', lat: 45.9333, lon: 20.0833})

// 3. Kreiraj puteve (obostrane veze između gradova) - prilagođeno modelu
CREATE (bg)-[:ROAD {distanceKm: 80.0, durationHours: 1.5, blocked: false, reason: null}]->(ns)
CREATE (ns)-[:ROAD {distanceKm: 80.0, durationHours: 1.5, blocked: false, reason: null}]->(bg)

CREATE (bg)-[:ROAD {distanceKm: 240.0, durationHours: 3.0, blocked: false, reason: null}]->(nis)
CREATE (nis)-[:ROAD {distanceKm: 240.0, durationHours: 3.0, blocked: false, reason: null}]->(bg)

CREATE (ns)-[:ROAD {distanceKm: 105.0, durationHours: 1.8, blocked: false, reason: null}]->(sub)
CREATE (sub)-[:ROAD {distanceKm: 105.0, durationHours: 1.8, blocked: false, reason: null}]->(ns)

CREATE (ns)-[:ROAD {distanceKm: 75.0, durationHours: 1.2, blocked: false, reason: null}]->(zr)
CREATE (zr)-[:ROAD {distanceKm: 75.0, durationHours: 1.2, blocked: false, reason: null}]->(ns)

CREATE (sub)-[:ROAD {distanceKm: 55.0, durationHours: 1.0, blocked: false, reason: null}]->(som)
CREATE (som)-[:ROAD {distanceKm: 55.0, durationHours: 1.0, blocked: false, reason: null}]->(sub)

CREATE (bg)-[:ROAD {distanceKm: 25.0, durationHours: 0.5, blocked: false, reason: null}]->(pancevo)
CREATE (pancevo)-[:ROAD {distanceKm: 25.0, durationHours: 0.5, blocked: false, reason: null}]->(bg)

CREATE (bg)-[:ROAD {distanceKm: 45.0, durationHours: 1.0, blocked: false, reason: null}]->(sm)
CREATE (sm)-[:ROAD {distanceKm: 45.0, durationHours: 1.0, blocked: false, reason: null}]->(bg)

CREATE (bg)-[:ROAD {distanceKm: 140.0, durationHours: 2.0, blocked: false, reason: null}]->(kg)
CREATE (kg)-[:ROAD {distanceKm: 140.0, durationHours: 2.0, blocked: false, reason: null}]->(bg)

CREATE (kg)-[:ROAD {distanceKm: 115.0, durationHours: 2.2, blocked: false, reason: null}]->(kraljevo)
CREATE (kraljevo)-[:ROAD {distanceKm: 115.0, durationHours: 2.2, blocked: false, reason: null}]->(kg)

CREATE (nis)-[:ROAD {distanceKm: 42.0, durationHours: 0.8, blocked: false, reason: null}]->(leskovac)
CREATE (leskovac)-[:ROAD {distanceKm: 42.0, durationHours: 0.8, blocked: false, reason: null}]->(nis)

CREATE (leskovac)-[:ROAD {distanceKm: 38.0, durationHours: 0.7, blocked: true, reason: 'Avala na putu'}]->(vranje)
CREATE (vranje)-[:ROAD {distanceKm: 38.0, durationHours: 0.7, blocked: true, reason: 'Avala na putu'}]->(leskovac)

CREATE (bg)-[:ROAD {distanceKm: 95.0, durationHours: 1.8, blocked: false, reason: null}]->(sabac)
CREATE (sabac)-[:ROAD {distanceKm: 95.0, durationHours: 1.8, blocked: false, reason: null}]->(bg)

CREATE (bg)-[:ROAD {distanceKm: 100.0, durationHours: 2.0, blocked: false, reason: null}]->(valjevo)
CREATE (valjevo)-[:ROAD {distanceKm: 100.0, durationHours: 2.0, blocked: false, reason: null}]->(bg)

CREATE (valjevo)-[:ROAD {distanceKm: 105.0, durationHours: 2.2, blocked: false, reason: null}]->(uzice)
CREATE (uzice)-[:ROAD {distanceKm: 105.0, durationHours: 2.2, blocked: false, reason: null}]->(valjevo)

CREATE (kg)-[:ROAD {distanceKm: 70.0, durationHours: 1.5, blocked: false, reason: null}]->(cacak)
CREATE (cacak)-[:ROAD {distanceKm: 70.0, durationHours: 1.5, blocked: false, reason: null}]->(kg)

CREATE (cacak)-[:ROAD {distanceKm: 40.0, durationHours: 1.0, blocked: false, reason: null}]->(pozega)
CREATE (pozega)-[:ROAD {distanceKm: 40.0, durationHours: 1.0, blocked: false, reason: null}]->(cacak)

CREATE (nis)-[:ROAD {distanceKm: 80.0, durationHours: 1.5, blocked: false, reason: null}]->(krusevac)
CREATE (krusevac)-[:ROAD {distanceKm: 80.0, durationHours: 1.5, blocked: false, reason: null}]->(nis)

CREATE (kg)-[:ROAD {distanceKm: 50.0, durationHours: 1.0, blocked: false, reason: null}]->(smedpalanka)
CREATE (smedpalanka)-[:ROAD {distanceKm: 50.0, durationHours: 1.0, blocked: false, reason: null}]->(kg)

// 4. Kreiraj vozila - samo polja koja postoje u modelu
CREATE (v1:Vozilo {marka: 'Mercedes', model: 'Actros', registracija: 'BG123AB', kapacitetKg: 20000.0, status: 'slobodno'})
CREATE (v2:Vozilo {marka: 'Volvo', model: 'FH16', registracija: 'NS456CD', kapacitetKg: 15000.0, status: 'slobodno'})
CREATE (v3:Vozilo {marka: 'MAN', model: 'TGX', registracija: 'NI789EF', kapacitetKg: 18000.0, status: 'na_servisu'})
CREATE (v4:Vozilo {marka: 'Iveco', model: 'Stralis', registracija: 'SU012GH', kapacitetKg: 12000.0, status: 'slobodno'})
CREATE (v5:Vozilo {marka: 'Scania', model: 'R450', registracija: 'PA345IJ', kapacitetKg: 25000.0, status: 'u_voznji'})
CREATE (v6:Vozilo {marka: 'Mercedes', model: 'Atego', registracija: 'KG678KL', kapacitetKg: 7000.0, status: 'slobodno'})
CREATE (v7:Vozilo {marka: 'Volvo', model: 'FMX', registracija: 'SM901MN', kapacitetKg: 16000.0, status: 'na_servisu'})
CREATE (v8:Vozilo {marka: 'Renault', model: 'T480', registracija: 'VR234OP', kapacitetKg: 14000.0, status: 'slobodno'})
CREATE (v9:Vozilo {marka: 'DAF', model: 'XF 480', registracija: 'KI567QR', kapacitetKg: 19000.0, status: 'u_voznji'})
CREATE (v10:Vozilo {marka: 'Scania', model: 'G450', registracija: 'SE890ST', kapacitetKg: 11000.0, status: 'slobodno'})

// 5. Kreiraj vozače - samo polja koja postoje u modelu
CREATE (vo1:Vozac {ime: 'Marko', prezime: 'Marković', brVoznji: 15, status: 'slobodan', email: 'marko.markovic@transport.com'})
CREATE (vo2:Vozac {ime: 'Petar', prezime: 'Petrović', brVoznji: 8, status: 'slobodan', email: 'petar.petrovic@transport.com'})
CREATE (vo3:Vozac {ime: 'Jovan', prezime: 'Jovanović', brVoznji: 22, status: 'na_odmoru', email: 'jovan.jovanovic@transport.com'})
CREATE (vo4:Vozac {ime: 'Ivan', prezime: 'Ivanović', brVoznji: 12, status: 'slobodan', email: 'ivan.ivanovic@transport.com'})
CREATE (vo5:Vozac {ime: 'Stefan', prezime: 'Stefanović', brVoznji: 30, status: 'u_voznji', email: 'stefan.stefanovic@transport.com'})
CREATE (vo6:Vozac {ime: 'Nikola', prezime: 'Nikolić', brVoznji: 5, status: 'slobodan', email: 'nikola.nikolic@transport.com'})
CREATE (vo7:Vozac {ime: 'Dejan', prezime: 'Dejanović', brVoznji: 18, status: 'bolovanje', email: 'dejan.dejanovic@transport.com'})
CREATE (vo8:Vozac {ime: 'Milan', prezime: 'Milanović', brVoznji: 25, status: 'slobodan', email: 'milan.milanovic@transport.com'})
CREATE (vo9:Vozac {ime: 'Dragan', prezime: 'Draganović', brVoznji: 9, status: 'slobodan', email: 'dragan.draganovic@transport.com'})
CREATE (vo10:Vozac {ime: 'Nemanja', prezime: 'Nemanjić', brVoznji: 35, status: 'u_voznji', email: 'nemanja.nemanjic@transport.com'})

// 6. Kreiraj rute - prilagođeno modelu (bez dodatnih polja)
CREATE (r1:Route {distanceKm: 80.0, durationHours: 1.5, status: 'planirana'})
CREATE (r1)-[:STARTS_AT]->(bg)
CREATE (r1)-[:ENDS_AT]->(ns)

CREATE (r2:Route {distanceKm: 240.0, durationHours: 3.0, status: 'planirana'})
CREATE (r2)-[:STARTS_AT]->(bg)
CREATE (r2)-[:ENDS_AT]->(nis)

CREATE (r3:Route {distanceKm: 105.0, durationHours: 1.8, status: 'aktivna'})
CREATE (r3)-[:STARTS_AT]->(ns)
CREATE (r3)-[:ENDS_AT]->(sub)

CREATE (r4:Route {distanceKm: 140.0, durationHours: 2.0, status: 'zavrsena'})
CREATE (r4)-[:STARTS_AT]->(bg)
CREATE (r4)-[:ENDS_AT]->(kg)

CREATE (r5:Route {distanceKm: 42.0, durationHours: 0.8, status: 'planirana'})
CREATE (r5)-[:STARTS_AT]->(nis)
CREATE (r5)-[:ENDS_AT]->(leskovac)

CREATE (r6:Route {distanceKm: 75.0, durationHours: 1.2, status: 'aktivna'})
CREATE (r6)-[:STARTS_AT]->(ns)
CREATE (r6)-[:ENDS_AT]->(zr)

CREATE (r7:Route {distanceKm: 115.0, durationHours: 2.2, status: 'otkazana'})
CREATE (r7)-[:STARTS_AT]->(kg)
CREATE (r7)-[:ENDS_AT]->(kraljevo)

CREATE (r8:Route {distanceKm: 95.0, durationHours: 1.8, status: 'planirana'})
CREATE (r8)-[:STARTS_AT]->(bg)
CREATE (r8)-[:ENDS_AT]->(sabac)

CREATE (r9:Route {distanceKm: 205.0, durationHours: 4.2, status: 'aktivna'})
CREATE (r9)-[:STARTS_AT]->(bg)
CREATE (r9)-[:ENDS_AT]->(uzice)

CREATE (r10:Route {distanceKm: 120.0, durationHours: 2.5, status: 'planirana'})
CREATE (r10)-[:STARTS_AT]->(nis)
CREATE (r10)-[:ENDS_AT]->(krusevac)

// Dodaj FOLLOWS_ROUTE veze za neke rute
CREATE (r9)-[:FOLLOWS_ROUTE]->(valjevo)
CREATE (r9)-[:FOLLOWS_ROUTE]->(pozega)

// 7. Kreiraj isporuke - samo polja koja postoje u modelu
CREATE (i1:Isporuka {kolicinaKg: 10000.0, status: 'aktivna', datumKreiranja: datetime('2025-12-05T08:00:00'), datumPolaska: null, datumDolaska:null})
CREATE (i2:Isporuka {kolicinaKg: 8000.0, status: 'u_toku', datumKreiranja: datetime('2025-12-04T10:30:00'), datumPolaska: datetime('2025-12-04T11:00:00'), datumDolaska:null})
CREATE (i3:Isporuka {kolicinaKg: 12000.0, status: 'zavrsena', datumKreiranja: datetime('2025-12-01T09:15:00'), datumPolaska: datetime('2025-12-01T10:00:00'), datumDolaska: datetime('2025-12-01T12:00:00')})
CREATE (i4:Isporuka {kolicinaKg: 6000.0, status: 'aktivna', datumKreiranja: datetime('2025-12-06T14:00:00'), datumPolaska: null, datumDolaska:null})
CREATE (i5:Isporuka {kolicinaKg: 16000.0, status: 'u_toku', datumKreiranja: datetime('2025-12-05T07:30:00'), datumPolaska: datetime('2025-12-05T08:00:00'), datumDolaska:null})
CREATE (i6:Isporuka {kolicinaKg: 5000.0, status: 'otkazana', datumKreiranja: datetime('2025-12-03T12:00:00'), datumPolaska: null, datumDolaska:null})
CREATE (i7:Isporuka {kolicinaKg: 14000.0, status: 'aktivna', datumKreiranja: datetime('2025-12-07T09:00:00'), datumPolaska: null, datumDolaska:null})
CREATE (i8:Isporuka {kolicinaKg: 9000.0, status: 'u_toku', datumKreiranja: datetime('2025-12-06T16:30:00'), datumPolaska: datetime('2025-12-06T17:00:00'),datumDolaska:null})
CREATE (i9:Isporuka {kolicinaKg: 18000.0, status: 'zavrsena', datumKreiranja: datetime('2025-11-28T08:45:00'), datumPolaska: datetime('2025-11-28T09:30:00'), datumDolaska: datetime('2025-11-28T14:00:00')})
CREATE (i10:Isporuka {kolicinaKg: 7500.0, status: 'planirana', datumKreiranja: datetime('2025-12-08T10:00:00'), datumPolaska: null, datumDolaska:null})

// 8. Kreiraj veze između isporuka i resursa
CREATE (i1)-[:USES_VEHICLE]->(v1)
CREATE (i1)-[:DRIVEN_BY]->(vo1)
CREATE (i1)-[:ON_ROUTE]->(r1)

CREATE (i2)-[:USES_VEHICLE]->(v2)
CREATE (i2)-[:DRIVEN_BY]->(vo2)
CREATE (i2)-[:ON_ROUTE]->(r2)

CREATE (i3)-[:USES_VEHICLE]->(v5)
CREATE (i3)-[:DRIVEN_BY]->(vo5)
CREATE (i3)-[:ON_ROUTE]->(r4)

CREATE (i4)-[:USES_VEHICLE]->(v4)
CREATE (i4)-[:DRIVEN_BY]->(vo4)
CREATE (i4)-[:ON_ROUTE]->(r3)

CREATE (i5)-[:USES_VEHICLE]->(v6)
CREATE (i5)-[:DRIVEN_BY]->(vo6)
CREATE (i5)-[:ON_ROUTE]->(r6)

CREATE (i7)-[:USES_VEHICLE]->(v8)
CREATE (i7)-[:DRIVEN_BY]->(vo8)
CREATE (i7)-[:ON_ROUTE]->(r8)

CREATE (i8)-[:USES_VEHICLE]->(v10)
CREATE (i8)-[:DRIVEN_BY]->(vo9)
CREATE (i8)-[:ON_ROUTE]->(r5)

CREATE (i9)-[:USES_VEHICLE]->(v9)
CREATE (i9)-[:DRIVEN_BY]->(vo10)
CREATE (i9)-[:ON_ROUTE]->(r9)

// 9. Kreiraj rampe - samo polja koja postoje u modelu
CREATE (rampa1:Rampa {oznaka: 'R01', status: 'slobodna'})
CREATE (rampa2:Rampa {oznaka: 'R02', status: 'zauzeta'})
CREATE (rampa3:Rampa {oznaka: 'R03', status: 'slobodna'})
CREATE (rampa4:Rampa {oznaka: 'R04', status: 'zauzeta'})
CREATE (rampa5:Rampa {oznaka: 'R05', status: 'na_odrzavanju'})
CREATE (rampa6:Rampa {oznaka: 'R06', status: 'slobodna'})
CREATE (rampa7:Rampa {oznaka: 'R07', status: 'slobodna'})

// Dodeli rampe isporukama
CREATE (rampa2)-[:ASSIGNED_TO]->(i1)
CREATE (rampa4)-[:ASSIGNED_TO]->(i3)
CREATE (rampa6)-[:ASSIGNED_TO]->(i5)