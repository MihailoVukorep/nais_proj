package com.example.demo.controller;

import com.example.demo.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    @Autowired
    private  ReportService reportService;

    @GetMapping("/save")
    public ResponseEntity<?> generateAndSaveReport() {
        try {
            //byte[] pdf = reportService.generateSimpleReportBytes();
            byte[] pdf = reportService.generateLogisticReport();

            String path = "C:/reports/logistic_report_" + System.currentTimeMillis() + ".pdf";

            reportService.saveReport(pdf, path);

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "path", path
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of(
                            "success", false,
                            "error", e.getMessage()
                    )
            );
        }
    }

    @GetMapping("/logistic")
    public ResponseEntity<byte[]> generateLogisticReport() {
        try {
            byte[] pdfBytes = reportService.generateSimpleReportBytes();

            String filename = "logistic_report_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";

            reportService.saveReport(pdfBytes, filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(("Greška: " + e.getMessage()).getBytes());
        }
    }

}
