package com.example.demo.service;

import com.example.demo.dto.DriverAnalyticsDTO;
import com.example.demo.dto.IsporukaReportDTO;
import com.example.demo.dto.RoadInfoDTO;
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
import java.time.LocalDateTime;
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
    @Autowired
    private RoadService roadService;

    public byte[] generateSimpleReportBytes() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, out);
            doc.open();

            // ===== SECTION 1 =====
            //doc.add(new Paragraph("SEKCIJA 1: Isporuke (poslednjih 30 dana)"));
            //doc.add(new Paragraph(" "));

            //String start = LocalDate.now().minusDays(30) + "T00:00:00";
            //List<IsporukaReportDTO> isporuke =
                    //isporukaRepository.findIsporukeFromDate(LocalDate.now().minusDays(30).toString());
                    //isporukaRepository.findIsporukeFromDate("\"2025-12-03\"");
            //List<Isporuka> isporuke =
              //      isporukaRepository.findIsporukeFromDate(LocalDate.now().minusDays(30));
                    //isporukaRepository.findIsporukeSimpleDTO(LocalDate.now().minusDays(30));
            /*PdfPTable table1 = new PdfPTable(4);
            table1.setWidthPercentage(100);
            table1.addCell("ID");
            table1.addCell("Status");
            table1.addCell("Količina");
            table1.addCell("Datum polaska");

            for (Isporuka i : isporuke) {
                table1.addCell("Isporuka "+ i.getId().toString());
                table1.addCell(i.getStatus());
                table1.addCell(i.getKolicinaKg().toString());
                table1.addCell(i.getDatumPolaska() != null ? i.getDatumPolaska().toString() : "-");
            }*/
            doc.add(new Paragraph("SEKCIJA 1: Kasnjenje isporuka zbog blokada na sledecim putevima:"));
            doc.add(new Paragraph(" "));

            List<RoadInfoDTO> roads = roadService.findBlocked();
            PdfPTable table1 = new PdfPTable(4);
            table1.setWidthPercentage(100);
            table1.addCell("Start");
            table1.addCell("Destinacija");
            table1.addCell("distanca(KM)");
            table1.addCell("Razlog blokade");

            for (RoadInfoDTO r : roads) {
                table1.addCell(r.getStart());
                table1.addCell(r.getDestination());
                table1.addCell(r.getDistanceKm().toString());
                table1.addCell(r.getReason() != null ? r.getReason().toString() : "-");
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
            Document doc = new Document(PageSize.A4, 50, 50, 80, 50);
            PdfWriter writer = PdfWriter.getInstance(doc, baos);

            writer.setPageEvent(new HeaderFooterHandler());

            doc.open();

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
                "• Pregled kašnjenja isporuka\n" +
                "• Analizu vozila za hitnu popravku\n" +
                "• Klasifikaciju vozača sa preporukama za bonuse\n\n" +
                "Generisano za: Logistički koordinator\n" +
                "Datum generisanja: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")), infoFont);
        info.setAlignment(Element.ALIGN_CENTER);
        doc.add(info);

        doc.newPage();
    }

    private void createIsporukeSection(Document doc) throws DocumentException {
        // Sekcija header
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(41, 128, 185));
        Paragraph sectionTitle = new Paragraph("SEKCIJA 1: PREGLED RAZLOGA KASNJENJA ISPORUKA I BLOKADA PRILIKOM TRANSPORTA", sectionFont);
        sectionTitle.setAlignment(Element.ALIGN_LEFT);
        sectionTitle.setSpacingBefore(20);
        sectionTitle.setSpacingAfter(15);
        doc.add(sectionTitle);

        // Uvodni tekst
        Font introFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
        Paragraph intro = new Paragraph("Ovaj odeljak prikazuje razloge kašnjenja isporuka prilikom transporta. " +
                "Podaci obuhvataju početnu i krajnju tačku, i udaljenost između njih zajedno za razlokom blokade.", introFont);
        intro.setSpacingAfter(20);
        doc.add(intro);

        // Dobavljanje podataka o isporukama
        //List<Isporuka> isporuke = isporukaRepository.findIsporukeFromDate(LocalDate.now().minusDays(30));
        //List<IsporukaReportDTO> isporuke = isporukaRepository.findIsporukeSimpleDTO(LocalDate.now().minusDays(30));

        List<RoadInfoDTO> roads = roadService.findBlocked();
        if (roads.isEmpty()) {
            Font warningFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.ORANGE);
            Paragraph warning = new Paragraph("NEMA PODATAKA.", warningFont);
            warning.setAlignment(Element.ALIGN_CENTER);
            warning.setSpacingBefore(20);
            warning.setSpacingAfter(20);
            doc.add(warning);

        } else {
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 2f, 1.5f, 4f});

            // Header tabele
            String[] headers = {"Start", "Destinacija", "Distanca(KM)", "Razlog blokade"};
            for (String header : headers) {
                PdfPCell headerCell = new PdfPCell(new Phrase(header,
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
                headerCell.setBackgroundColor(new Color(41, 128, 185));
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                headerCell.setPadding(8);
                table.addCell(headerCell);
            }


            //for (Isporuka i : isporuke) {
            for (RoadInfoDTO r : roads) {
                table.addCell(createStyledCell(r.getStart(), Element.ALIGN_CENTER, Color.BLACK));
                table.addCell(createStyledCell(r.getDestination(), Element.ALIGN_CENTER, Color.BLACK));

                double dist = r.getDistanceKm() != null ? r.getDistanceKm() : 0;
                table.addCell(createStyledCell(String.format("%.2f", dist), Element.ALIGN_CENTER, Color.BLACK));
                table.addCell(createStyledCell(r.getReason() != null ? r.getReason().toString() : "-", Element.ALIGN_CENTER, Color.BLACK));

            }

            doc.add(table);

            doc.add(Chunk.NEWLINE);
            createStatisticsBox(doc, "STATISTIKA", new String[]{
                    String.format("Ukupan broj kašnjenja: %d", roads.size()),
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

        List<Vozilo> vozilaUKvaru = voziloRepository.findByStatusAndMinKapacitet("u_kvaru", 1000.0);

        List<Vozilo> vozilaNaServisu = voziloRepository.findByStatusAndMinKapacitet("na_servisu",1000.0);


        if (!vozilaUKvaru.isEmpty()) {
            Font subheaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(220, 53, 69));
            Paragraph subheader = new Paragraph("VOZILA U KVARU (KAPACITET > 1000 kg)", subheaderFont);
            subheader.setSpacingBefore(10);
            subheader.setSpacingAfter(10);
            doc.add(subheader);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            //table.setWidths(new float[]{1.5f, 1.5f, 1.5f, 1f, 1.5f, 1.5f, 2f});

            String[] headers = {"Registracija", "Marka", "Model", "Kapacitet (kg)"};
            for (String header : headers) {
                table.addCell(createHeaderCell(header));
            }

            for (Vozilo v : vozilaUKvaru) {
                table.addCell(createStyledCell(v.getRegistracija(), Element.ALIGN_CENTER, Color.BLACK));
                table.addCell(createStyledCell(v.getMarka(), Element.ALIGN_CENTER, Color.BLACK));
                table.addCell(createStyledCell(v.getModel(), Element.ALIGN_CENTER, Color.BLACK));
                table.addCell(createStyledCell(String.format("%.2f", v.getKapacitetKg()),
                        Element.ALIGN_CENTER, Color.RED, true));
            }

            doc.add(table);

            createWarningBox(doc, "HITNA INTERVENCIJA",
                    "Ova vozila su neispravna i zahtevaju hitan servis. " +
                            "Svaki dan zastoja ovih vozila košta kompaniju u gubitku prihoda.",
                    Color.RED);
        }

        if (!vozilaNaServisu.isEmpty()) {
            Font subheaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(253, 126, 20));
            Paragraph subheader = new Paragraph("VOZILA NA SERVISU (KAPACITET > 1000 kg)", subheaderFont);
            subheader.setSpacingBefore(20);
            subheader.setSpacingAfter(10);
            doc.add(subheader);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            //table.setWidths(new float[]{1.5f, 1.5f, 1.5f, 1f, 1.5f, 1.5f, 2f});

            String[] headers = {"Registracija", "Marka", "Model", "Kapacitet (kg)"};
            for (String header : headers) {
                table.addCell(createHeaderCell(header));
            }

            for (Vozilo v : vozilaNaServisu) {
                table.addCell(createStyledCell(v.getRegistracija(), Element.ALIGN_CENTER, Color.BLACK));
                table.addCell(createStyledCell(v.getMarka(), Element.ALIGN_CENTER, Color.BLACK));
                table.addCell(createStyledCell(v.getModel(), Element.ALIGN_CENTER, Color.BLACK));
                table.addCell(createStyledCell(String.format("%.2f", v.getKapacitetKg()),
                        Element.ALIGN_CENTER, Color.RED, true));

            }

            doc.add(table);

            createWarningBox(doc, "PREPORUKA",
                    "Ova vozila su trenutno na servisu. Preporučuje se praćenje trajanja servisa " +
                            "i planiranje zamenskih vozila ako servis traje duže od planiranog.",
                    new Color(253, 126, 20));
        }

        if (vozilaUKvaru.isEmpty() && vozilaNaServisu.isEmpty()) {
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
                    bonus = "BONUS: 300€";
                } else if (rang == 2) {
                    rangColor = new Color(192, 192, 192); // Srebrna
                    bonus = "BONUS: 150€";
                } else if (rang == 3) {
                    rangColor = new Color(205, 127, 50); // Bronzana
                    bonus = "BONUS: 100€";
                } else if (rang <= 5) {
                    rangColor = new Color(144, 238, 144); // Sv. zelena
                    bonus = "BONUS: 70€";
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
                    "🥇 1. mesto: 300€ (Zlatna boja)",
                    "🥈 2. mesto: 150€ (Srebrna boja)",
                    "🥉 3. mesto: 100€ (Bronzana boja)",
                    "🏆 4.-5. mesto: 70€ (Sv. zelena boja)",
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

        PdfPCell titleCell = new PdfPCell(new Phrase(title,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
        titleCell.setBackgroundColor(new Color(41, 128, 185));
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setPadding(10);
        titleCell.setBorderColor(Color.WHITE);
        statsTable.addCell(titleCell);

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

                cb.showTextAligned(PdfContentByte.ALIGN_RIGHT,
                        LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                        document.right(), document.bottom() - 40, 0);

                cb.endText();
                cb.restoreState();
            } catch (Exception e) {

            }
        }
    }
}

