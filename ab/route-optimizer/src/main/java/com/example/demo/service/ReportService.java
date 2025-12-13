package com.example.demo.service;

import com.example.demo.dto.DriverAnalyticsDTO;
import com.example.demo.dto.IsporukaReportDTO;
import com.example.demo.model.Isporuka;
import com.example.demo.model.Vozac;
import com.example.demo.model.Vozilo;
import com.example.demo.repository.IsporukaRepository;
import com.example.demo.repository.VoziloRepository;
import com.example.demo.repository.VozacRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    @Autowired
    private IsporukaRepository isporukaRepository;
    @Autowired
    private VoziloRepository voziloRepository;
    @Autowired
    private VozacRepository vozacRepository;

    public byte[] generateSimpleReportBytes() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, out);
            doc.open();

            // ===== SECTION 1 =====
            doc.add(new Paragraph("SEKCIJA 1: Isporuke (poslednjih 30 dana)"));
            doc.add(new Paragraph(" "));

            //String start = LocalDate.now().minusDays(30) + "T00:00:00";
            List<IsporukaReportDTO> isporuke =
                    //isporukaRepository.findIsporukeFromDate(LocalDate.now().minusDays(30).toString());
                    //isporukaRepository.findIsporukeFromDate("\"2025-12-03\"");
                    //isporukaRepository.findIsporukeFromDate(LocalDate.now().minusDays(30));
                    isporukaRepository.findIsporukeSimpleDTO(LocalDate.now().minusDays(30));
            PdfPTable table1 = new PdfPTable(4);
            table1.setWidthPercentage(100);
            table1.addCell("ID");
            table1.addCell("Status");
            table1.addCell("Količina");
            table1.addCell("Datum polaska");

            for (IsporukaReportDTO i : isporuke) {
                table1.addCell("Isporuka "+ i.getId().toString());
                table1.addCell(i.getStatus());
                table1.addCell(i.getKolicinaKg().toString());
                table1.addCell(i.getDatumPolaska() != null ? i.getDatumPolaska().toString() : "-");
                /*table1.addCell(String.valueOf(i.getId()));
                table1.addCell(String.valueOf(i.getStatus()));
                table1.addCell(String.valueOf(i.getKolicinaKg()));
                table1.addCell(i.getDatumPolaska() != null ? i.getDatumPolaska().toString() : "-");*/
            }

            doc.add(table1);
            doc.newPage();

            // ===== SECTION 2 =====
            doc.add(new Paragraph("SEKCIJA 2: Vozila (slobodna, min kapacitet 1000kg)"));
            doc.add(new Paragraph(" "));

            List<Vozilo> vozila =
                    voziloRepository.findByStatusAndMinKapacitet("slobodno", 1000.0);

            PdfPTable table2 = new PdfPTable(4);
            table2.setWidthPercentage(100);
            table2.addCell("Registracija");
            table2.addCell("Marka");
            table2.addCell("Model");
            table2.addCell("Kapacitet");

            for (Vozilo v : vozila) {
                table2.addCell(v.getRegistracija());
                table2.addCell(v.getMarka());
                table2.addCell(v.getModel());
                table2.addCell(String.valueOf(v.getKapacitetKg()));
            }

            doc.add(table2);
            doc.newPage();

            // ===== SECTION 3 =====
            doc.add(new Paragraph("SEKCIJA 3: Analiza vozača"));
            doc.add(new Paragraph(" "));

            List<DriverAnalyticsDTO> drivers =
                    vozacRepository.recommendDrivers(
                            LocalDate.now().minusDays(60).toString(),
                            LocalDate.now().toString(),
                            10
                    );

            PdfPTable table3 = new PdfPTable(4);
            table3.setWidthPercentage(100);
            table3.addCell("Ime");
            table3.addCell("Prezime");
            table3.addCell("Vožnji");
            table3.addCell("Prosek rute");

            for (DriverAnalyticsDTO m : drivers) {
                /*table3.addCell(String.valueOf(m.get("ime")));
                table3.addCell(String.valueOf(m.get("prezime")));
                table3.addCell(String.valueOf(m.get("brVoznji")));
                table3.addCell(String.valueOf(m.get("avgRouteDistance")));*/
                table3.addCell(String.valueOf(m.getIme()));
                table3.addCell(String.valueOf(m.getPrezime()));
                table3.addCell(String.valueOf(m.getBrVoznji()));
                table3.addCell(String.valueOf(m.getAvgRouteDistance()));
            }

            doc.add(table3);
            doc.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Greška pri generisanju PDF izveštaja: " + e.getMessage(), e);
        }
    }
    public void saveReport(byte[] pdfBytes, String filePath) {
        try {
            File file = new File(filePath);

            // napravi folder ako ne postoji
            file.getParentFile().mkdirs();

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(pdfBytes);
            }

        } catch (Exception e) {
            throw new RuntimeException("Greška pri čuvanju PDF fajla: " + e.getMessage(), e);
        }
    }

    public byte[] generateLogisticReport() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            // Kreiranje dokumenta sa većim marginama za profesionalniji izgled
            Document doc = new Document(PageSize.A4, 50, 50, 80, 50);
            PdfWriter writer = PdfWriter.getInstance(doc, baos);

            // Dodavanje custom headera i footera
            writer.setPageEvent(new HeaderFooterHandler());

            doc.open();

            // === NASLOVNA STRANA ===
            createTitlePage(doc);

            // === SEKCIJA 1: PREGLED ISPORUKA U PRETHODNIH 30 DANA ===
            createIsporukeSection(doc);

            // === SEKCIJA 2: VOZILA ZA HITNU POPRAVKU ===
            createVozilaSection(doc);

            // === SEKCIJA 3: ANALIZA I KLASIFIKACIJA VOZAČA ===
            createVozaciSection(doc);

            doc.close();

        } catch (Exception e) {
            throw new RuntimeException("Greška pri generisanju PDF izveštaja: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }

    private void createTitlePage(Document doc) throws DocumentException {
        // Logo ili naslovna slika (možete dodati logo ako ga imate)
        try {
            Image logo = Image.getInstance("src/main/resources/static/logo.png");
            logo.scaleToFit(150, 100);
            logo.setAbsolutePosition((PageSize.A4.getWidth() - 150) / 2, PageSize.A4.getHeight() - 150);
            doc.add(logo);
        } catch (Exception e) {
            // Ako nema logo fajla, nastavimo bez njega
        }

        // Glavni naslov
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 32, new Color(0, 51, 102));
        Paragraph title = new Paragraph("OPERATIVNI IZVEŠTAJ LOGISTIČKOG KOORDINATORA", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(200);
        doc.add(title);

        // Podnaslov
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 18, new Color(102, 102, 102));
        Paragraph subtitle = new Paragraph("Analiza performansi flote za period: " +
                LocalDate.now().minusDays(30).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) +
                " - " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingBefore(20);
        doc.add(subtitle);

        // Informacije o izveštaju
        Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.DARK_GRAY);
        Paragraph info = new Paragraph("\n\n\n\n\n\n\n" +
                "Izveštaj sadrži:\n" +
                "• Pregled isporuka u prethodnih 30 dana\n" +
                "• Analizu vozila za hitnu popravku\n" +
                "• Klasifikaciju vozača sa preporukama za bonuse\n\n" +
                "Generisano za: Logistički koordinator\n" +
                "Datum generisanja: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")), infoFont);
        info.setAlignment(Element.ALIGN_CENTER);
        doc.add(info);

        // Pečat ili potpis
        Font stampFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.GRAY);
        Paragraph stamp = new Paragraph("\n\n\n\n\n\nOvaj izveštaj je automatski generisan i ne zahteva fizički potpis.", stampFont);
        stamp.setAlignment(Element.ALIGN_CENTER);
        doc.add(stamp);

        doc.newPage();
    }

    private void createIsporukeSection(Document doc) throws DocumentException {
        // Sekcija header
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(41, 128, 185));
        Paragraph sectionTitle = new Paragraph("SEKCIJA 1: PREGLED ISPORUKA U PRETHODNIH 30 DANA", sectionFont);
        sectionTitle.setAlignment(Element.ALIGN_LEFT);
        sectionTitle.setSpacingBefore(20);
        sectionTitle.setSpacingAfter(15);
        doc.add(sectionTitle);

        // Uvodni tekst
        Font introFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
        Paragraph intro = new Paragraph("Ovaj odeljak prikazuje sve isporuke izvršene u periodu od prethodnih 30 dana. " +
                "Podaci obuhvataju status isporuke, količinu tereta, vreme polaska i dolaska, kao i dodeljenu rutu.", introFont);
        intro.setSpacingAfter(20);
        doc.add(intro);

        // Dobavljanje podataka o isporukama
        //List<Isporuka> isporuke = isporukaRepository.findIsporukeFromDate(LocalDate.now().minusDays(30));
        List<IsporukaReportDTO> isporuke = isporukaRepository.findIsporukeSimpleDTO(LocalDate.now().minusDays(30));

        if (isporuke.isEmpty()) {
            Font warningFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.ORANGE);
            Paragraph warning = new Paragraph("NEMA PODATAKA O ISPORUKAMA ZA TRAŽENI PERIOD", warningFont);
            warning.setAlignment(Element.ALIGN_CENTER);
            warning.setSpacingBefore(20);
            warning.setSpacingAfter(20);
            doc.add(warning);

            // Dodajemo napomenu da verovatno nema isporuka ili je problem sa upitom
            Font noteFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Paragraph note = new Paragraph("Napomena: Proverite da li postoje isporuke u bazi za period od " +
                    LocalDate.now().minusDays(30).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) +
                    " do " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), noteFont);
            note.setAlignment(Element.ALIGN_CENTER);
            doc.add(note);
        } else {
            // Kreiranje tabele za isporuke
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.8f, 1.2f, 1.5f, 2f, 2f, 1.5f, 1f});

            // Header tabele
            String[] headers = {"ID", "Količina (kg)", "Status", "Vreme polaska", "Vreme dolaska"};
            for (String header : headers) {
                PdfPCell headerCell = new PdfPCell(new Phrase(header,
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
                headerCell.setBackgroundColor(new Color(41, 128, 185));
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                headerCell.setPadding(8);
                table.addCell(headerCell);
            }

            // Popunjavanje tabele podacima
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            double ukupnaKolicina = 0;
            int brojIsporucenih = 0;
            int brojHitnih = 0;

            //for (Isporuka i : isporuke) {
            for (IsporukaReportDTO i : isporuke) {
                // ID
                table.addCell(createStyledCell("IS-" + i.getId(), Element.ALIGN_CENTER, Color.BLACK));

                // Količina
                double kolicina = i.getKolicinaKg() != null ? i.getKolicinaKg() : 0;
                table.addCell(createStyledCell(String.format("%.2f", kolicina), Element.ALIGN_CENTER, Color.BLACK));
                ukupnaKolicina += kolicina;

                // Status sa bojom
                String status = i.getStatus() != null ? i.getStatus() : "NEPOZNATO";
                Color statusColor = getStatusColor(status);
                table.addCell(createStyledCell(status.toUpperCase(), Element.ALIGN_CENTER, statusColor, true));

                if ("ISPORUCENA".equalsIgnoreCase(status)) {
                    brojIsporucenih++;
                }

                // Vreme polaska
                String polazak = i.getDatumPolaska() != null ?
                        i.getDatumPolaska().format(formatter) : "N/A";
                table.addCell(createStyledCell(polazak, Element.ALIGN_CENTER, Color.BLACK));

                // Vreme dolaska
                String dolazak = i.getDatumDolaska() != null ?
                        i.getDatumDolaska().format(formatter) : "U TOKU";
                table.addCell(createStyledCell(dolazak, Element.ALIGN_CENTER, Color.BLACK));

            }

            doc.add(table);

            // Statistika isporuka
            doc.add(Chunk.NEWLINE);
            createStatisticsBox(doc, "STATISTIKA ISPORUKA", new String[]{
                    String.format("Ukupan broj isporuka: %d", isporuke.size()),
                    String.format("Isporučeno: %d (%.1f%%)", brojIsporucenih,
                            (isporuke.size() > 0 ? (brojIsporucenih * 100.0 / isporuke.size()) : 0)),
                    String.format("Ukupna količina tereta: %.2f kg", ukupnaKolicina),
                    String.format("Hitnih isporuka: %d", brojHitnih),
                    String.format("Prosečna količina po isporuci: %.2f kg",
                            (isporuke.size() > 0 ? ukupnaKolicina / isporuke.size() : 0))
            }, new Color(230, 240, 255));
        }

        doc.newPage();
    }

    private void createVozilaSection(Document doc) throws DocumentException {
        // Sekcija header
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(220, 53, 69));
        Paragraph sectionTitle = new Paragraph("SEKCIJA 2: VOZILA ZA HITNU POPRAVKU", sectionFont);
        sectionTitle.setAlignment(Element.ALIGN_LEFT);
        sectionTitle.setSpacingBefore(20);
        sectionTitle.setSpacingAfter(15);
        doc.add(sectionTitle);

        // Uvodni tekst
        Font introFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
        Paragraph intro = new Paragraph("Ova sekcija identifikuje vozila koja su u kvaru ili na servisu, a imaju kapacitet veći od 1000 kg. " +
                "Ova vozila su kritična za operacije jer se koriste za prenos većih tereta. " +
                "Preporučuje se hitna intervencija kako bi se smanjio zastoj u operacijama.", introFont);
        intro.setSpacingAfter(20);
        doc.add(intro);

        // Dobavljanje vozila sa statusom u kvaru ili na servisu
        List<Vozilo> vozilaUKvaru = voziloRepository.findByStatus("u_kvaru");
        List<Vozilo> vozilaNaServisu = voziloRepository.findByStatus("na_servisu");

        // Filtriranje vozila sa kapacitetom > 1000 kg
        List<Vozilo> kritickaVozilaUKvaru = vozilaUKvaru.stream()
                .filter(v -> v.getKapacitetKg() > 1000.0)
                .toList();

        List<Vozilo> kritickaVozilaNaServisu = vozilaNaServisu.stream()
                .filter(v -> v.getKapacitetKg() > 1000.0)
                .toList();

        // Kreiranje tabele za vozila u kvaru
        if (!kritickaVozilaUKvaru.isEmpty()) {
            Font subheaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(220, 53, 69));
            Paragraph subheader = new Paragraph("VOZILA U KVARU (KAPACITET > 1000 kg)", subheaderFont);
            subheader.setSpacingBefore(10);
            subheader.setSpacingAfter(10);
            doc.add(subheader);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 1.5f, 1.5f, 1f, 1.5f, 1.5f, 2f});

            String[] headers = {"Registracija", "Marka", "Model", "Kapacitet (kg)", "Registracija"};
            for (String header : headers) {
                table.addCell(createHeaderCell(header));
            }

            for (Vozilo v : kritickaVozilaUKvaru) {
                table.addCell(createStyledCell(v.getRegistracija(), Element.ALIGN_CENTER, Color.BLACK));
                table.addCell(createStyledCell(v.getMarka(), Element.ALIGN_CENTER, Color.BLACK));
                table.addCell(createStyledCell(v.getModel(), Element.ALIGN_CENTER, Color.BLACK));
                table.addCell(createStyledCell(String.format("%.2f", v.getKapacitetKg()),
                        Element.ALIGN_CENTER, Color.RED, true));
                table.addCell(createStyledCell(v.getRegistracija(),
                        Element.ALIGN_CENTER, Color.BLACK));
            }

            doc.add(table);

            // Upozorenje za vozila u kvaru
            createWarningBox(doc, "HITNA INTERVENCIJA",
                    "Ova vozila su neispravna i zahtevaju hitan servis. " +
                            "Svaki dan zastoja ovih vozila košta kompaniju u gubitku prihoda.",
                    Color.RED);
        }

        // Kreiranje tabele za vozila na servisu
        if (!kritickaVozilaNaServisu.isEmpty()) {
            Font subheaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(253, 126, 20));
            Paragraph subheader = new Paragraph("VOZILA NA SERVISU (KAPACITET > 1000 kg)", subheaderFont);
            subheader.setSpacingBefore(20);
            subheader.setSpacingAfter(10);
            doc.add(subheader);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 1.5f, 1.5f, 1f, 1.5f, 1.5f, 2f});

            String[] headers = {"Registracija", "Marka", "Model", "Kapacitet (kg)", "Registracija"};
            for (String header : headers) {
                table.addCell(createHeaderCell(header));
            }

            for (Vozilo v : kritickaVozilaNaServisu) {
                table.addCell(createStyledCell(v.getRegistracija(), Element.ALIGN_CENTER, Color.BLACK));
                table.addCell(createStyledCell(v.getMarka(), Element.ALIGN_CENTER, Color.BLACK));
                table.addCell(createStyledCell(v.getModel(), Element.ALIGN_CENTER, Color.BLACK));
                table.addCell(createStyledCell(String.format("%.2f", v.getKapacitetKg()),
                        Element.ALIGN_CENTER, Color.RED, true));
                table.addCell(createStyledCell(v.getRegistracija(),
                        Element.ALIGN_CENTER, Color.BLACK));
            }

            doc.add(table);

            // Preporuka za vozila na servisu
            createWarningBox(doc, "PREPORUKA",
                    "Ova vozila su trenutno na servisu. Preporučuje se praćenje trajanja servisa " +
                            "i planiranje zamenskih vozila ako servis traje duže od planiranog.",
                    new Color(253, 126, 20));
        }

        if (kritickaVozilaUKvaru.isEmpty() && kritickaVozilaNaServisu.isEmpty()) {
            Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.GREEN);
            Paragraph info = new Paragraph("NEMA KRITIČNIH VOZILA ZA POPRAVKU", infoFont);
            info.setAlignment(Element.ALIGN_CENTER);
            info.setSpacingBefore(20);
            info.setSpacingAfter(20);
            doc.add(info);
        }

        doc.newPage();
    }

    private void createVozaciSection(Document doc) throws DocumentException {
        // Sekcija header
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(40, 167, 69));
        Paragraph sectionTitle = new Paragraph("SEKCIJA 3: KLASIFIKACIJA I ANALIZA VOZAČA", sectionFont);
        sectionTitle.setAlignment(Element.ALIGN_LEFT);
        sectionTitle.setSpacingBefore(20);
        sectionTitle.setSpacingAfter(15);
        doc.add(sectionTitle);

        // Uvodni tekst
        Font introFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
        Paragraph intro = new Paragraph("Ova sekcija prikazuje performanse vozača u poslednjih 60 dana. " +
                "Vozači su rangirani na osnovu broja vožnji i prosečne dužine rute. " +
                "Top 3 vozača dobijaju bonuse u zavisnosti od pozicije na rang listi.", introFont);
        intro.setSpacingAfter(20);
        doc.add(intro);

        // Dobavljanje podataka o vozačima
        List<DriverAnalyticsDTO> vozaci = vozacRepository.recommendDrivers(
                LocalDate.now().minusDays(60).toString(),
                LocalDate.now().toString(),
                10
        );

        if (vozaci.isEmpty()) {
            Font warningFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.ORANGE);
            Paragraph warning = new Paragraph("NEMA PODATAKA O VOZAČIMA ZA TRAŽENI PERIOD", warningFont);
            warning.setAlignment(Element.ALIGN_CENTER);
            warning.setSpacingBefore(20);
            warning.setSpacingAfter(20);
            doc.add(warning);
        } else {
            // Tabela sa svim vozačima
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 1.5f, 1.2f, 1.5f, 1.8f, 2f});

            String[] headers = {"Rang", "Ime", "Prezime", "Broj vožnji", "Prosek rute (km)", "Preporuka za bonus"};
            for (String header : headers) {
                table.addCell(createHeaderCell(header));
            }

            for (int i = 0; i < vozaci.size(); i++) {
                DriverAnalyticsDTO v = vozaci.get(i);
                int rang = i + 1;

                // Boja za rang
                Color rangColor;
                String bonus;
                if (rang == 1) {
                    rangColor = new Color(255, 215, 0); // Zlatna
                    bonus = "BONUS: 500€";
                } else if (rang == 2) {
                    rangColor = new Color(192, 192, 192); // Srebrna
                    bonus = "BONUS: 300€";
                } else if (rang == 3) {
                    rangColor = new Color(205, 127, 50); // Bronzana
                    bonus = "BONUS: 200€";
                } else if (rang <= 5) {
                    rangColor = new Color(144, 238, 144); // Sv. zelena
                    bonus = "BONUS: 100€";
                } else if (rang <= 10) {
                    rangColor = new Color(173, 216, 230); // Sv. plava
                    bonus = "BONUS: 50€";
                } else {
                    rangColor = Color.LIGHT_GRAY;
                    bonus = "STANDARD";
                }

                // Rang
                table.addCell(createStyledCell(String.valueOf(rang), Element.ALIGN_CENTER, rangColor, true));

                // Ime
                table.addCell(createStyledCell(v.getIme() != null ? v.getIme() : "",
                        Element.ALIGN_CENTER, Color.BLACK));

                // Prezime
                table.addCell(createStyledCell(v.getPrezime() != null ? v.getPrezime() : "",
                        Element.ALIGN_CENTER, Color.BLACK));

                // Broj vožnji
                int brVoznji = v.getBrVoznji() != null ? v.getBrVoznji() : 0;
                table.addCell(createStyledCell(String.valueOf(brVoznji),
                        Element.ALIGN_CENTER, Color.BLACK, brVoznji > 20));

                // Prosek rute
                double avgDistance = v.getAvgRouteDistance() != null ? v.getAvgRouteDistance() : 0;
                table.addCell(createStyledCell(String.format("%.1f km", avgDistance),
                        Element.ALIGN_CENTER, Color.BLACK, avgDistance > 100));

                // Preporuka za bonus
                table.addCell(createStyledCell(bonus, Element.ALIGN_CENTER,
                        rang <= 3 ? Color.RED : Color.BLACK, rang <= 3));
            }

            doc.add(table);

            // Objašnjenje bonus sistema
            doc.add(Chunk.NEWLINE);
            createStatisticsBox(doc, "SISTEM BONUSA ZA VOZAČE", new String[]{
                    "🥇 1. mesto: 500€ (Zlatna boja)",
                    "🥈 2. mesto: 300€ (Srebrna boja)",
                    "🥉 3. mesto: 200€ (Bronzana boja)",
                    "🏆 4.-5. mesto: 100€ (Sv. zelena boja)",
                    "⭐ 6.-10. mesto: 50€ (Sv. plava boja)",
                    "📊 Ostali: Standardni uslovi"
            }, new Color(240, 255, 240));

            // Zaključak i preporuke
            doc.add(Chunk.NEWLINE);
            Font conclusionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(0, 51, 102));
            Paragraph conclusionTitle = new Paragraph("ZAKLJUČAK I PREPORUKE", conclusionFont);
            conclusionTitle.setSpacingBefore(10);
            conclusionTitle.setSpacingAfter(10);
            doc.add(conclusionTitle);

            Font recommendationFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
            Paragraph recommendations = new Paragraph(
                    "1. Najbolji vozači (rang 1-3) treba da budu nagrađeni kako bi se motivisali i drugi.\n" +
                            "2. Vozači sa najvišim brojem vožnji treba da budu prioritet za kompleksnije rute.\n" +
                            "3. Vozači sa niskim performansama treba da prođu dodatnu obuku.\n" +
                            "4. Sistem bonusa treba revidirati svakog kvartala na osnovu novih podataka.", recommendationFont);
            recommendations.setSpacingAfter(20);
            doc.add(recommendations);
        }
    }

    // ================= POMOĆNE METODE ==================

    private PdfPCell createStyledCell(String text, int alignment, Color color) {
        return createStyledCell(text, alignment, color, false);
    }

    private PdfPCell createStyledCell(String text, int alignment, Color color, boolean bold) {
        Font font = bold ?
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, color) :
                FontFactory.getFont(FontFactory.HELVETICA, 10, color);

        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        cell.setBorderColor(Color.LIGHT_GRAY);
        return cell;
    }

    private PdfPCell createHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE)));
        cell.setBackgroundColor(new Color(51, 122, 183));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);
        cell.setBorderColor(Color.WHITE);
        return cell;
    }

    private Color getStatusColor(String status) {
        if (status == null) return Color.GRAY;

        switch (status.toUpperCase()) {
            case "ISPORUCENA":
                return new Color(40, 167, 69); // Zelena
            case "U TRANSPORTU":
                return new Color(0, 123, 255); // Plava
            case "U PRIJEMU":
                return new Color(255, 193, 7); // Žuta
            case "OTKAZANA":
                return new Color(220, 53, 69); // Crvena
            default:
                return Color.GRAY;
        }
    }

    private void createStatisticsBox(Document doc, String title, String[] items, Color bgColor) throws DocumentException {
        PdfPTable statsTable = new PdfPTable(1);
        statsTable.setWidthPercentage(100);
        statsTable.setSpacingBefore(10);
        statsTable.setSpacingAfter(10);

        // Naslov box-a
        PdfPCell titleCell = new PdfPCell(new Phrase(title,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
        titleCell.setBackgroundColor(new Color(41, 128, 185));
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setPadding(10);
        titleCell.setBorderColor(Color.WHITE);
        statsTable.addCell(titleCell);

        // Stavke
        for (String item : items) {
            PdfPCell itemCell = new PdfPCell(new Phrase(item,
                    FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK)));
            itemCell.setBackgroundColor(bgColor);
            itemCell.setPadding(8);
            itemCell.setBorderColor(Color.LIGHT_GRAY);
            statsTable.addCell(itemCell);
        }

        doc.add(statsTable);
    }

    private void createWarningBox(Document doc, String title, String message, Color color) throws DocumentException {
        PdfPTable warningTable = new PdfPTable(1);
        warningTable.setWidthPercentage(100);
        warningTable.setSpacingBefore(10);
        warningTable.setSpacingAfter(10);

        // Naslov upozorenja
        PdfPCell titleCell = new PdfPCell(new Phrase("⚠ " + title,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
        titleCell.setBackgroundColor(color);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setPadding(10);
        titleCell.setBorderColor(color);
        warningTable.addCell(titleCell);

        // Poruka upozorenja
        PdfPCell messageCell = new PdfPCell(new Phrase(message,
                FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK)));
        messageCell.setBackgroundColor(new Color(255, 243, 205)); // Svetlo žuta
        messageCell.setPadding(12);
        messageCell.setBorderColor(color);
        warningTable.addCell(messageCell);

        doc.add(warningTable);
    }

    // ================= HEADER I FOOTER HANDLER ==================

    private static class HeaderFooterHandler extends PdfPageEventHelper {

        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte cb = writer.getDirectContent();
                cb.saveState();

                // Header linija
                cb.setColorStroke(new Color(41, 128, 185));
                cb.setLineWidth(1.5f);
                cb.moveTo(document.left(), document.top() + 10);
                cb.lineTo(document.right(), document.top() + 10);
                cb.stroke();

                // Header tekst
                BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.EMBEDDED);
                cb.beginText();
                cb.setFontAndSize(bf, 10);
                cb.setColorFill(new Color(41, 128, 185));
                cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "LOGISTIČKI IZVEŠTAJ",
                        document.left(), document.top() + 20, 0);
                cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, "FlotoManager Pro",
                        document.right(), document.top() + 20, 0);
                cb.endText();

                cb.restoreState();
            } catch (Exception e) {
                // Ignoriši greške u header-u
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte cb = writer.getDirectContent();
                cb.saveState();

                // Footer linija
                cb.setColorStroke(new Color(41, 128, 185));
                cb.setLineWidth(1);
                cb.moveTo(document.left(), document.bottom() - 30);
                cb.lineTo(document.right(), document.bottom() - 30);
                cb.stroke();

                // Footer tekst
                BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
                cb.beginText();
                cb.setFontAndSize(bf, 9);
                cb.setColorFill(Color.GRAY);

                // Levi footer
                cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
                        "Interna dokumentacija - Zabranjeno kopiranje",
                        document.left(), document.bottom() - 40, 0);

                // Srednji footer
                String pageInfo = "Strana " + writer.getPageNumber() + " od ";
                try {
                    pageInfo += writer.getPageNumber(); // Za jednostavnost, u praksi bi bilo writer.getPageNumber() + " od " + totalPages
                } catch (Exception e) {
                    pageInfo += "?";
                }
                cb.showTextAligned(PdfContentByte.ALIGN_CENTER, pageInfo,
                        (document.left() + document.right()) / 2, document.bottom() - 40, 0);

                // Desni footer
                cb.showTextAligned(PdfContentByte.ALIGN_RIGHT,
                        LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                        document.right(), document.bottom() - 40, 0);

                cb.endText();
                cb.restoreState();
            } catch (Exception e) {
                // Ignoriši greške u footer-u
            }
        }
    }
}
    /*public void generateLogisticReport(HttpServletResponse response) {
        try {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=logistic_report.pdf");

            Document doc = new Document(PageSize.A4, 40, 40, 60, 40);
            PdfWriter writer = PdfWriter.getInstance(doc, response.getOutputStream());

            writer.setPageEvent(new FooterHandler());

            doc.open();

            // === COVER PAGE ===
            Paragraph title = new Paragraph("LOGISTIČKI IZVEŠTAJ",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24));
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(150);

            Paragraph subtitle = new Paragraph("Isporuke • Vozila • Analitika vozača",
                    FontFactory.getFont(FontFactory.HELVETICA, 14));
            subtitle.setAlignment(Element.ALIGN_CENTER);

            Paragraph date = new Paragraph("Datum generisanja: " + LocalDate.now(),
                    FontFactory.getFont(FontFactory.HELVETICA, 12));
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingBefore(20);

            doc.add(title);
            doc.add(subtitle);
            doc.add(date);

            doc.newPage();

            // === SECTION 1 — SIMPLE ===
            addSectionHeader(doc, "1. Isporuke po datumu polaska");

            // koristiš svoj query sa datetime($start) → šaljemo ISO string
            List<Isporuka> isporuke =
                    isporukaRepository.findIsporukeFromDate(LocalDate.now().minusDays(30).toString());

            PdfPTable t1 = new PdfPTable(new float[]{1, 1, 1, 1, 1, 1});
            t1.setWidthPercentage(100);

            addTableHeader(t1, new String[]{"ID", "Količina", "Status", "Polazak", "Dolazak", "Ruta"});

            for (Isporuka i : isporuke) {
                t1.addCell(cell(i.getId().toString()));
                t1.addCell(cell(i.getKolicinaKg() + " kg"));
                t1.addCell(cell(i.getStatus()));
                t1.addCell(cell(i.getDatumPolaska().toString()));
                t1.addCell(cell(i.getDatumDolaska() != null ? i.getDatumDolaska().toString() : "-"));
                t1.addCell(cell("Ruta "+ i.getRoute().getId().toString()));
            }

            doc.add(t1);
            doc.newPage();

            // === SECTION 2 — SIMPLE ===
            addSectionHeader(doc, "2. Vozila po statusu i minimalnom kapacitetu");

            List<Vozilo> vozila =
                    voziloRepository.findByStatusAndMinKapacitet("slobodno", 1000.0);

            PdfPTable t2 = new PdfPTable(new float[]{1.3f, 1, 1, 1, 1});
            t2.setWidthPercentage(100);

            addTableHeader(t2, new String[]{"Registracija", "Marka", "Model", "Kapacitet", "Status"});

            for (Vozilo v : vozila) {
                t2.addCell(cell(v.getRegistracija()));
                t2.addCell(cell(v.getMarka()));
                t2.addCell(cell(v.getModel()));
                t2.addCell(cell(v.getKapacitetKg() + " kg"));

                Color color = switch (v.getStatus()) {
                    case "u_kvaru" -> Color.RED;
                    case "na_servisu" -> Color.ORANGE;
                    default -> Color.GREEN;
                };

                PdfPCell statusCell = new PdfPCell(new Phrase(v.getStatus()));
                statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                statusCell.setPhrase(new Phrase(v.getStatus(),
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, color)));

                t2.addCell(statusCell);
            }

            doc.add(t2);

            // warnings
            boolean imaKvar = vozila.stream().anyMatch(v -> v.getStatus().equals("u_kvaru"));
            boolean imaServis = vozila.stream().anyMatch(v -> v.getStatus().equals("na_servisu"));

            if (imaKvar) {
                doc.add(new Paragraph("❗ Hitna napomena: Postoje vozila u kvaru!",
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.RED)));
            }
            if (imaServis) {
                doc.add(new Paragraph("⚠ Napomena: Neka vozila su na servisu.",
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.ORANGE)));
            }

            doc.newPage();

            // === SECTION 3 — COMPLEX ===
            addSectionHeader(doc, "3. Analiza vozača i preporuka");

            List<Map<String, Object>> vozaci =
                    vozacRepository.recommendDrivers(
                            LocalDate.now().minusDays(60).toString(),
                            LocalDate.now().toString(),
                            10
                    );

            PdfPTable t3 = new PdfPTable(new float[]{1, 1, 1, 1});
            t3.setWidthPercentage(100);

            addTableHeader(t3, new String[]{"Ime", "Prezime", "Vožnji", "Prosek rute"});

            for (Map<String, Object> m : vozaci) {
                t3.addCell(cell(m.get("ime").toString()));
                t3.addCell(cell(m.get("prezime").toString()));
                t3.addCell(cell(m.get("brVoznji").toString()));
                t3.addCell(cell(m.get("avgRouteDistance") + " km"));
            }

            doc.add(t3);

            if (!vozaci.isEmpty()) {
                Map<String, Object> best = vozaci.get(0);
                doc.add(new Paragraph(
                        "Preporučeni vozač: " + best.get("ime") + " " + best.get("prezime"),
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLUE)
                ));
            }

            doc.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= UTILITIES ==================

    private void addSectionHeader(Document doc, String text) {
        Paragraph p = new Paragraph(text,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLUE));
        p.setSpacingAfter(10);
        doc.add(p);
    }

    private PdfPCell cell(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        return c;
    }

    private void addTableHeader(PdfPTable table, String[] headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            cell.setBackgroundColor(new Color(220, 220, 220));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    // ================= FOOTER ==================

    private static class FooterHandler extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Rectangle rect = writer.getPageSize();

            Phrase footer = new Phrase(
                    "Strana " + writer.getPageNumber(),
                    FontFactory.getFont(FontFactory.HELVETICA, 9)
            );

            ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_CENTER,
                    footer,
                    rect.getWidth() / 2,   // sredina strane
                    20,                    // 20px od dna
                    0
            );
        }
    }*/

