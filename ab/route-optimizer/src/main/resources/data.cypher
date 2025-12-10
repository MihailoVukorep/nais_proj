// 1. Brisanje postojećih podataka
MATCH (n) DETACH DELETE n;

// 2. Kreiraj lokacije (gradovi i mesta u Srbiji)
CREATE (bg:Location {name: 'Beograd', lat: 44.7866, lon: 20.4489, type: 'grad'})
CREATE (ns:Location {name: 'Novi Sad', lat: 45.2671, lon: 19.8335, type: 'grad'})
CREATE (nis:Location {name: 'Niš', lat: 43.3209, lon: 21.8958, type: 'grad'})
CREATE (kg:Location {name: 'Kragujevac', lat: 44.0128, lon: 20.9114, type: 'grad'})
CREATE (sub:Location {name: 'Subotica', lat: 46.1005, lon: 19.6655, type: 'grad'})
CREATE (zr:Location {name: 'Zrenjanin', lat: 45.3836, lon: 20.3819, type: 'grad'})
CREATE (som:Location {name: 'Sombor', lat: 45.7731, lon: 19.1142, type: 'grad'})
CREATE (pancevo:Location {name: 'Pančevo', lat: 44.874, lon: 20.647, type: 'grad'})
CREATE (sm:Location {name: 'Smederevo', lat: 44.6659, lon: 20.9396, type: 'grad'})
CREATE (kraljevo:Location {name: 'Kraljevo', lat: 43.7259, lon: 20.6896, type: 'grad'})
CREATE (leskovac:Location {name: 'Leskovac', lat: 42.9976, lon: 21.9445, type: 'grad'})
CREATE (vranje:Location {name: 'Vranje', lat: 42.5524, lon: 21.9003, type: 'grad'})
CREATE (uzice:Location {name: 'Užice', lat: 43.8556, lon: 19.8425, type: 'grad'})
CREATE (cacak:Location {name: 'Čačak', lat: 43.8914, lon: 20.3497, type: 'grad'})
CREATE (valjevo:Location {name: 'Valjevo', lat: 44.2725, lon: 19.8875, type: 'grad'})
CREATE (sabac:Location {name: 'Šabac', lat: 44.7566, lon: 19.6909, type: 'grad'})
CREATE (pozega:Location {name: 'Požega', lat: 43.8456, lon: 20.0369, type: 'grad'})
CREATE (krusevac:Location {name: 'Kruševac', lat: 43.5800, lon: 21.3339, type: 'grad'})
CREATE (paracin:Location {name: 'Paraćin', lat: 43.8608, lon: 21.4117, type: 'grad'})
CREATE (smedpalanka:Location {name: 'Smederevska Palanka', lat: 44.3667, lon: 20.9667, type: 'grad'})
CREATE (indjija:Location {name: 'Indjija', lat: 45.0481, lon: 20.0894, type: 'grad'})
CREATE (ruma:Location {name: 'Ruma', lat: 45.0081, lon: 19.8222, type: 'grad'})
CREATE (stpazova:Location {name: 'Stara Pazova', lat: 44.9850, lon: 20.1603, type: 'grad'})
CREATE (becej:Location {name: 'Bečej', lat: 45.6167, lon: 20.0333, type: 'grad'})
CREATE (vrsac:Location {name: 'Vršac', lat: 45.1236, lon: 21.2983, type: 'grad'})
CREATE (kikinda:Location {name: 'Kikinda', lat: 45.8289, lon: 20.4653, type: 'grad'})
CREATE (senta:Location {name: 'Senta', lat: 45.9333, lon: 20.0833, type: 'grad'})

// 3. Kreiraj puteve (obostrane veze između gradova) - glavni putevi
// Autoput E-75 deonica
CREATE (bg)-[:ROAD {distanceKm: 80, durationHours: 1.5, blocked: false, type: 'autoput', name: 'A1'}]->(ns)
CREATE (ns)-[:ROAD {distanceKm: 80, durationHours: 1.5, blocked: false, type: 'autoput', name: 'A1'}]->(bg)

CREATE (bg)-[:ROAD {distanceKm: 240, durationHours: 3.0, blocked: false, type: 'autoput', name: 'A1'}]->(nis)
CREATE (nis)-[:ROAD {distanceKm: 240, durationHours: 3.0, blocked: false, type: 'autoput', name: 'A1'}]->(bg)

// Regionalni putevi
CREATE (ns)-[:ROAD {distanceKm: 105, durationHours: 1.8, blocked: false, type: 'regionalni', name: 'R-101'}]->(sub)
CREATE (sub)-[:ROAD {distanceKm: 105, durationHours: 1.8, blocked: false, type: 'regionalni', name: 'R-101'}]->(ns)

CREATE (ns)-[:ROAD {distanceKm: 75, durationHours: 1.2, blocked: false, type: 'regionalni', name: 'R-120'}]->(zr)
CREATE (zr)-[:ROAD {distanceKm: 75, durationHours: 1.2, blocked: false, type: 'regionalni', name: 'R-120'}]->(ns)

CREATE (sub)-[:ROAD {distanceKm: 55, durationHours: 1.0, blocked: false, type: 'regionalni', name: 'R-102'}]->(som)
CREATE (som)-[:ROAD {distanceKm: 55, durationHours: 1.0, blocked: false, type: 'regionalni', name: 'R-102'}]->(sub)

CREATE (bg)-[:ROAD {distanceKm: 25, durationHours: 0.5, blocked: false, type: 'regionalni', name: 'R-104'}]->(pancevo)
CREATE (pancevo)-[:ROAD {distanceKm: 25, durationHours: 0.5, blocked: false, type: 'regionalni', name: 'R-104'}]->(bg)

CREATE (bg)-[:ROAD {distanceKm: 45, durationHours: 1.0, blocked: false, type: 'regionalni', name: 'R-113'}]->(sm)
CREATE (sm)-[:ROAD {distanceKm: 45, durationHours: 1.0, blocked: false, type: 'regionalni', name: 'R-113'}]->(bg)

CREATE (bg)-[:ROAD {distanceKm: 140, durationHours: 2.0, blocked: false, type: 'regionalni', name: 'R-22'}]->(kg)
CREATE (kg)-[:ROAD {distanceKm: 140, durationHours: 2.0, blocked: false, type: 'regionalni', name: 'R-22'}]->(bg)

CREATE (kg)-[:ROAD {distanceKm: 115, durationHours: 2.2, blocked: false, type: 'regionalni', name: 'R-23'}]->(kraljevo)
CREATE (kraljevo)-[:ROAD {distanceKm: 115, durationHours: 2.2, blocked: false, type: 'regionalni', name: 'R-23'}]->(kg)

CREATE (nis)-[:ROAD {distanceKm: 42, durationHours: 0.8, blocked: false, type: 'regionalni', name: 'R-35'}]->(leskovac)
CREATE (leskovac)-[:ROAD {distanceKm: 42, durationHours: 0.8, blocked: false, type: 'regionalni', name: 'R-35'}]->(nis)

CREATE (leskovac)-[:ROAD {distanceKm: 38, durationHours: 0.7, blocked: true, reason: 'Avala na putu', type: 'regionalni', name: 'R-243'}]->(vranje)
CREATE (vranje)-[:ROAD {distanceKm: 38, durationHours: 0.7, blocked: true, reason: 'Avala na putu', type: 'regionalni', name: 'R-243'}]->(leskovac)

CREATE (bg)-[:ROAD {distanceKm: 95, durationHours: 1.8, blocked: false, type: 'regionalni', name: 'R-21'}]->(sabac)
CREATE (sabac)-[:ROAD {distanceKm: 95, durationHours: 1.8, blocked: false, type: 'regionalni', name: 'R-21'}]->(bg)

CREATE (bg)-[:ROAD {distanceKm: 100, durationHours: 2.0, blocked: false, type: 'regionalni', name: 'R-26'}]->(valjevo)
CREATE (valjevo)-[:ROAD {distanceKm: 100, durationHours: 2.0, blocked: false, type: 'regionalni', name: 'R-26'}]->(bg)

CREATE (valjevo)-[:ROAD {distanceKm: 105, durationHours: 2.2, blocked: false, type: 'regionalni', name: 'R-28'}]->(uzice)
CREATE (uzice)-[:ROAD {distanceKm: 105, durationHours: 2.2, blocked: false, type: 'regionalni', name: 'R-28'}]->(valjevo)

CREATE (kg)-[:ROAD {distanceKm: 70, durationHours: 1.5, blocked: false, type: 'regionalni', name: 'R-24'}]->(cacak)
CREATE (cacak)-[:ROAD {distanceKm: 70, durationHours: 1.5, blocked: false, type: 'regionalni', name: 'R-24'}]->(kg)

CREATE (cacak)-[:ROAD {distanceKm: 40, durationHours: 1.0, blocked: false, type: 'regionalni', name: 'R-25'}]->(pozega)
CREATE (pozega)-[:ROAD {distanceKm: 40, durationHours: 1.0, blocked: false, type: 'regionalni', name: 'R-25'}]->(cacak)

CREATE (nis)-[:ROAD {distanceKm: 80, durationHours: 1.5, blocked: false, type: 'regionalni', name: 'R-34'}]->(krusevac)
CREATE (krusevac)-[:ROAD {distanceKm: 80, durationHours: 1.5, blocked: false, type: 'regionalni', name: 'R-34'}]->(nis)

CREATE (kg)-[:ROAD {distanceKm: 50, durationHours: 1.0, blocked: false, type: 'regionalni', name: 'R-27'}]->(smedpalanka)
CREATE (smedpalanka)-[:ROAD {distanceKm: 50, durationHours: 1.0, blocked: false, type: 'regionalni', name: 'R-27'}]->(kg)

// 4. Kreiraj vozila (kamioni različitih marki i specifikacija)
CREATE (v1:Vozilo {
  marka: 'Mercedes', model: 'Actros', registracija: 'BG123AB',
  kapacitetKg: 20000.0, status: 'slobodno', godiste: 2020,
  tipGoriva: 'dizel', potrosnja: 30.5, predenoKm: 125000
})

CREATE (v2:Vozilo {
  marka: 'Volvo', model: 'FH16', registracija: 'NS456CD',
  kapacitetKg: 15000.0, status: 'slobodno', godiste: 2021,
  tipGoriva: 'dizel', potrosnja: 28.0, predenoKm: 85000
})

CREATE (v3:Vozilo {
  marka: 'MAN', model: 'TGX', registracija: 'NI789EF',
  kapacitetKg: 18000.0, status: 'na_servisu', godiste: 2019,
  tipGoriva: 'dizel', potrosnja: 32.0, predenoKm: 195000
})

CREATE (v4:Vozilo {
  marka: 'Iveco', model: 'Stralis', registracija: 'SU012GH',
  kapacitetKg: 12000.0, status: 'slobodno', godiste: 2022,
  tipGoriva: 'dizel', potrosnja: 26.5, predenoKm: 45000
})

CREATE (v5:Vozilo {
  marka: 'Scania', model: 'R450', registracija: 'PA345IJ',
  kapacitetKg: 25000.0, status: 'u_voznji', godiste: 2020,
  tipGoriva: 'dizel', potrosnja: 35.0, predenoKm: 150000
})

CREATE (v6:Vozilo {
  marka: 'Mercedes', model: 'Atego', registracija: 'KG678KL',
  kapacitetKg: 7000.0, status: 'slobodno', godiste: 2021,
  tipGoriva: 'dizel', potrosnja: 22.0, predenoKm: 75000
})

CREATE (v7:Vozilo {
  marka: 'Volvo', model: 'FMX', registracija: 'SM901MN',
  kapacitetKg: 16000.0, status: 'na_servisu', godiste: 2018,
  tipGoriva: 'dizel', potrosnja: 29.0, predenoKm: 220000
})

CREATE (v8:Vozilo {
  marka: 'Renault', model: 'T480', registracija: 'VR234OP',
  kapacitetKg: 14000.0, status: 'slobodno', godiste: 2023,
  tipGoriva: 'dizel', potrosnja: 27.5, predenoKm: 25000
})

CREATE (v9:Vozilo {
  marka: 'DAF', model: 'XF 480', registracija: 'KI567QR',
  kapacitetKg: 19000.0, status: 'u_voznji', godiste: 2021,
  tipGoriva: 'dizel', potrosnja: 31.0, predenoKm: 90000
})

CREATE (v10:Vozilo {
  marka: 'Scania', model: 'G450', registracija: 'SE890ST',
  kapacitetKg: 11000.0, status: 'slobodno', godiste: 2022,
  tipGoriva: 'dizel', potrosnja: 25.0, predenoKm: 40000
})

// 5. Kreiraj vozače
CREATE (vo1:Vozac {
  ime: 'Marko', prezime: 'Marković', brVoznji: 15,
  status: 'slobodan', kategorija: 'C+E', godiste: 1985,
  telefon: '0631234567', email: 'marko.markovic@transport.com'
})

CREATE (vo2:Vozac {
  ime: 'Petar', prezime: 'Petrović', brVoznji: 8,
  status: 'slobodan', kategorija: 'C', godiste: 1990,
  telefon: '0642345678', email: 'petar.petrovic@transport.com'
})

CREATE (vo3:Vozac {
  ime: 'Jovan', prezime: 'Jovanović', brVoznji: 22,
  status: 'na_odmoru', kategorija: 'C+E', godiste: 1978,
  telefon: '0653456789', email: 'jovan.jovanovic@transport.com'
})

CREATE (vo4:Vozac {
  ime: 'Ivan', prezime: 'Ivanović', brVoznji: 12,
  status: 'slobodan', kategorija: 'C', godiste: 1988,
  telefon: '0664567890', email: 'ivan.ivanovic@transport.com'
})

CREATE (vo5:Vozac {
  ime: 'Stefan', prezime: 'Stefanović', brVoznji: 30,
  status: 'u_voznji', kategorija: 'C+E', godiste: 1975,
  telefon: '0615678901', email: 'stefan.stefanovic@transport.com'
})

CREATE (vo6:Vozac {
  ime: 'Nikola', prezime: 'Nikolić', brVoznji: 5,
  status: 'slobodan', kategorija: 'C', godiste: 1995,
  telefon: '0626789012', email: 'nikola.nikolic@transport.com'
})

CREATE (vo7:Vozac {
  ime: 'Dejan', prezime: 'Dejanović', brVoznji: 18,
  status: 'bolovanje', kategorija: 'C+E', godiste: 1982,
  telefon: '0637890123', email: 'dejan.dejanovic@transport.com'
})

CREATE (vo8:Vozac {
  ime: 'Milan', prezime: 'Milanović', brVoznji: 25,
  status: 'slobodan', kategorija: 'C+E', godiste: 1980,
  telefon: '0648901234', email: 'milan.milanovic@transport.com'
})

CREATE (vo9:Vozac {
  ime: 'Dragan', prezime: 'Draganović', brVoznji: 9,
  status: 'slobodan', kategorija: 'C', godiste: 1992,
  telefon: '0659012345', email: 'dragan.draganovic@transport.com'
})

CREATE (vo10:Vozac {
  ime: 'Nemanja', prezime: 'Nemanjić', brVoznji: 35,
  status: 'u_voznji', kategorija: 'C+E', godiste: 1972,
  telefon: '0660123456', email: 'nemanja.nemanjic@transport.com'
})

// 6. Kreiraj rute (sa STARTS_AT i ENDS_AT vezama)
CREATE (r1:Route {
  distanceKm: 80.0,
  durationHours: 1.5,
  status: 'planirana',
  tezina: 'lak',
  vremePolaska: '08:00',
  napomena: 'Standardna ruta'
})

CREATE (r1)-[:STARTS_AT]->(bg)
CREATE (r1)-[:ENDS_AT]->(ns)

CREATE (r2:Route {
  distanceKm: 240.0,
  durationHours: 3.0,
  status: 'planirana',
  tezina: 'tezak',
  vremePolaska: '10:00',
  napomena: 'Duga ruta, obavezno punjenje goriva'
})

CREATE (r2)-[:STARTS_AT]->(bg)
CREATE (r2)-[:ENDS_AT]->(nis)

CREATE (r3:Route {
  distanceKm: 105.0,
  durationHours: 1.8,
  status: 'aktivna',
  tezina: 'lak',
  vremePolaska: '09:30',
  napomena: 'Regionalna ruta'
})

CREATE (r3)-[:STARTS_AT]->(ns)
CREATE (r3)-[:ENDS_AT]->(sub)

CREATE (r4:Route {
  distanceKm: 140.0,
  durationHours: 2.0,
  status: 'zavrsena',
  tezina: 'srednji',
  vremePolaska: '07:45',
  napomena: 'Uspesno zavrsena'
})

CREATE (r4)-[:STARTS_AT]->(bg)
CREATE (r4)-[:ENDS_AT]->(kg)

CREATE (r5:Route {
  distanceKm: 42.0,
  durationHours: 0.8,
  status: 'planirana',
  tezina: 'lak',
  vremePolaska: '14:00',
  napomena: 'Kratka ruta'
})

CREATE (r5)-[:STARTS_AT]->(nis)
CREATE (r5)-[:ENDS_AT]->(leskovac)

CREATE (r6:Route {
  distanceKm: 75.0,
  durationHours: 1.2,
  status: 'aktivna',
  tezina: 'lak',
  vremePolaska: '11:15',
  napomena: 'Prenos hrane'
})

CREATE (r6)-[:STARTS_AT]->(ns)
CREATE (r6)-[:ENDS_AT]->(zr)

CREATE (r7:Route {
  distanceKm: 115.0,
  durationHours: 2.2,
  status: 'otkazana',
  tezina: 'srednji',
  vremePolaska: '13:00',
  napomena: 'Otkazana zbog vremenskih uslova'
})

CREATE (r7)-[:STARTS_AT]->(kg)
CREATE (r7)-[:ENDS_AT]->(kraljevo)

CREATE (r8:Route {
  distanceKm: 95.0,
  durationHours: 1.8,
  status: 'planirana',
  tezina: 'srednji',
  vremePolaska: '12:30',
  napomena: 'Prenos elektroopreme'
})

CREATE (r8)-[:STARTS_AT]->(bg)
CREATE (r8)-[:ENDS_AT]->(sabac)

CREATE (r9:Route {
  distanceKm: 205.0,
  durationHours: 4.2,
  status: 'aktivna',
  tezina: 'tezak',
  vremePolaska: '06:00',
  napomena: 'Kombinovana ruta BG-Valjevo-Užice'
})

CREATE (r9)-[:STARTS_AT]->(bg)
CREATE (r9)-[:ENDS_AT]->(uzice)

CREATE (r10:Route {
  distanceKm: 120.0,
  durationHours: 2.5,
  status: 'planirana',
  tezina: 'srednji',
  vremePolaska: '15:45',
  napomena: 'Prenos gradevinskog materijala'
})

CREATE (r10)-[:STARTS_AT]->(nis)
CREATE (r10)-[:ENDS_AT]->(krusevac)

// Dodaj FOLLOWS_ROUTE veze za neke rute
CREATE (r9)-[:FOLLOWS_ROUTE]->(valjevo)
CREATE (r9)-[:FOLLOWS_ROUTE]->(pozega)

// 7. Kreiraj isporuke
CREATE (i1:Isporuka {
  kolicinaKg: 10000.0,
  status: 'aktivna',
  datumKreiranja: datetime('2025-12-05T08:00:00'),
  rokIsporuke: date('2025-12-10'),
  opis: 'Prenos nameštaja',
  prioritet: 'normalan',
  vrednostRobe: 50000.0
})

CREATE (i2:Isporuka {
  kolicinaKg: 8000.0,
  status: 'u_toku',
  datumKreiranja: datetime('2025-12-04T10:30:00'),
  datumPolaska: datetime('2025-12-04T11:00:00'),
  rokIsporuke: date('2025-12-06'),
  opis: 'Hrana za supermarkete',
  prioritet: 'visok',
  vrednostRobe: 35000.0
})

CREATE (i3:Isporuka {
  kolicinaKg: 12000.0,
  status: 'zavrsena',
  datumKreiranja: datetime('2025-12-01T09:15:00'),
  datumPolaska: datetime('2025-12-01T10:00:00'),
  datumDolaska: datetime('2025-12-01T12:00:00'),
  rokIsporuke: date('2025-12-02'),
  opis: 'Elektronika',
  prioritet: 'normalan',
  vrednostRobe: 75000.0
})

CREATE (i4:Isporuka {
  kolicinaKg: 6000.0,
  status: 'aktivna',
  datumKreiranja: datetime('2025-12-06T14:00:00'),
  rokIsporuke: date('2025-12-09'),
  opis: 'Kancelarijski materijal',
  prioritet: 'nizak',
  vrednostRobe: 15000.0
})

CREATE (i5:Isporuka {
  kolicinaKg: 16000.0,
  status: 'u_toku',
  datumKreiranja: datetime('2025-12-05T07:30:00'),
  datumPolaska: datetime('2025-12-05T08:00:00'),
  rokIsporuke: date('2025-12-07'),
  opis: 'Građevinski materijal',
  prioritet: 'visok',
  vrednostRobe: 45000.0
})

CREATE (i6:Isporuka {
  kolicinaKg: 5000.0,
  status: 'otkazana',
  datumKreiranja: datetime('2025-12-03T12:00:00'),
  rokIsporuke: date('2025-12-05'),
  opis: 'Tekstilna roba',
  prioritet: 'normalan',
  vrednostRobe: 20000.0,
  razlogOtkaza: 'Oštećena roba'
})

CREATE (i7:Isporuka {
  kolicinaKg: 14000.0,
  status: 'aktivna',
  datumKreiranja: datetime('2025-12-07T09:00:00'),
  rokIsporuke: date('2025-12-12'),
  opis: 'Farmaceutski proizvodi',
  prioritet: 'hitno',
  vrednostRobe: 120000.0
})

CREATE (i8:Isporuka {
  kolicinaKg: 9000.0,
  status: 'u_toku',
  datumKreiranja: datetime('2025-12-06T16:30:00'),
  datumPolaska: datetime('2025-12-06T17:00:00'),
  rokIsporuke: date('2025-12-08'),
  opis: 'Kozmetika',
  prioritet: 'normalan',
  vrednostRobe: 40000.0
})

CREATE (i9:Isporuka {
  kolicinaKg: 18000.0,
  status: 'zavrsena',
  datumKreiranja: datetime('2025-11-28T08:45:00'),
  datumPolaska: datetime('2025-11-28T09:30:00'),
  datumDolaska: datetime('2025-11-28T14:00:00'),
  rokIsporuke: date('2025-11-30'),
  opis: 'Automobilski delovi',
  prioritet: 'visok',
  vrednostRobe: 85000.0
})

CREATE (i10:Isporuka {
  kolicinaKg: 7500.0,
  status: 'planirana',
  datumKreiranja: datetime('2025-12-08T10:00:00'),
  rokIsporuke: date('2025-12-15'),
  opis: 'Knjige i štampani materijal',
  prioritet: 'nizak',
  vrednostRobe: 25000.0
})

// 8. Kreiraj veze između isporuka i resursa (VOZILA, VOZAČI, RUTE)
// i1 - aktivna isporuka
CREATE (i1)-[:USES_VEHICLE]->(v1)
CREATE (i1)-[:DRIVEN_BY]->(vo1)
CREATE (i1)-[:ON_ROUTE]->(r1)

// i2 - u toku
CREATE (i2)-[:USES_VEHICLE]->(v2)
CREATE (i2)-[:DRIVEN_BY]->(vo2)
CREATE (i2)-[:ON_ROUTE]->(r2)

// i3 - zavrsena
CREATE (i3)-[:USES_VEHICLE]->(v5)
CREATE (i3)-[:DRIVEN_BY]->(vo5)
CREATE (i3)-[:ON_ROUTE]->(r4)

// i4 - aktivna
CREATE (i4)-[:USES_VEHICLE]->(v4)
CREATE (i4)-[:DRIVEN_BY]->(vo4)
CREATE (i4)-[:ON_ROUTE]->(r3)

// i5 - u toku
CREATE (i5)-[:USES_VEHICLE]->(v6)
CREATE (i5)-[:DRIVEN_BY]->(vo6)
CREATE (i5)-[:ON_ROUTE]->(r6)

// i6 - otkazana (nema dodeljenih resursa)

// i7 - aktivna (hitna)
CREATE (i7)-[:USES_VEHICLE]->(v8)
CREATE (i7)-[:DRIVEN_BY]->(vo8)
CREATE (i7)-[:ON_ROUTE]->(r8)

// i8 - u toku
CREATE (i8)-[:USES_VEHICLE]->(v10)
CREATE (i8)-[:DRIVEN_BY]->(vo9)
CREATE (i8)-[:ON_ROUTE]->(r5)

// i9 - zavrsena
CREATE (i9)-[:USES_VEHICLE]->(v9)
CREATE (i9)-[:DRIVEN_BY]->(vo10)
CREATE (i9)-[:ON_ROUTE]->(r9)

// i10 - planirana (još nema dodeljene resurse)

// 9. Kreiraj rampe
CREATE (rampa1:Rampa {
  oznaka: 'R01',
  status: 'slobodna',
  lokacija: 'Beograd',
  terminal: 'Terminal A',
  dimenzije: '12x3m'
})

CREATE (rampa2:Rampa {
  oznaka: 'R02',
  status: 'zauzeta',
  lokacija: 'Beograd',
  terminal: 'Terminal A',
  dimenzije: '12x3m'
})

CREATE (rampa3:Rampa {
  oznaka: 'R03',
  status: 'slobodna',
  lokacija: 'Novi Sad',
  terminal: 'Terminal B',
  dimenzije: '10x3m'
})

CREATE (rampa4:Rampa {
  oznaka: 'R04',
  status: 'zauzeta',
  lokacija: 'Niš',
  terminal: 'Terminal C',
  dimenzije: '15x3m'
})

CREATE (rampa5:Rampa {
  oznaka: 'R05',
  status: 'na_odrzavanju',
  lokacija: 'Kragujevac',
  terminal: 'Terminal D',
  dimenzije: '12x3m'
})

CREATE (rampa6:Rampa {
  oznaka: 'R06',
  status: 'slobodna',
  lokacija: 'Subotica',
  terminal: 'Terminal E',
  dimenzije: '10x3m'
})

CREATE (rampa7:Rampa {
  oznaka: 'R07',
  status: 'slobodna',
  lokacija: 'Zrenjanin',
  terminal: 'Terminal F',
  dimenzije: '8x3m'
})

// Dodeli rampe isporukama
CREATE (rampa2)-[:ASSIGNED_TO]->(i1)
CREATE (rampa4)-[:ASSIGNED_TO]->(i3)
CREATE (rampa6)-[:ASSIGNED_TO]->(i5)