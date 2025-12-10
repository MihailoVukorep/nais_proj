package com.example.demo.service;

import com.example.demo.dto.CreateVoziloRequest;
import com.example.demo.dto.DjangoVoziloDTO;
import com.example.demo.dto.OrchestratorResponse;
import com.example.demo.model.Vozilo;
import com.example.demo.repository.VoziloRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrchestratorService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private VoziloRepository voziloRepository;

    //@Value("${external.django.base-url}")
    //@Value("${external.django.base-url:http://django:8000}")
    @Value("${external.django.base-url:http://localhost:8000}")
    private String djangoBaseUrl;

    private static final Logger log = LoggerFactory.getLogger(OrchestratorService.class);

    public OrchestratorResponse createAndRegisterVozilo(CreateVoziloRequest req) {
        String createUrl = djangoBaseUrl + "/api/vozila/create/"; // prilagodi API putanje
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String,Object> payload = Map.of(
                "marka", req.getMarka(),
                "model", req.getModel(),
                "registracija", req.getRegistracija(),
                //"registracija", LocalDateTime.now().plusYears(1).toLocalDate(),  //u django je polje registracija datum
                //"registracija", LocalDate.now().plusYears(1),  //u django je polje registracija datum
                "kapacitet", req.getKapacitetKg(),
                "status", req.getStatus()
        );
        Map<String,Object> payloadDjango = Map.of(
                "marka", req.getMarka(),
                "model", req.getModel(),
                //"registracija", req.getRegistracija(),
                "registracija", "2026-12-12",  //u django je polje registracija datum
               // "registracija", LocalDate.now().plusYears(1),  //u django je polje registracija datum
                "kapacitet", req.getKapacitetKg(),
                "status", req.getStatus()
        );

        HttpEntity<Map<String,Object>> httpEntity = new HttpEntity<>(payload, headers);
        HttpEntity<Map<String,Object>> httpEntityDjango = new HttpEntity<>(payloadDjango, headers);

        ResponseEntity<DjangoVoziloDTO> djangoResp;
        try {
            djangoResp = restTemplate.exchange(createUrl, HttpMethod.POST, httpEntityDjango, DjangoVoziloDTO.class);
        } catch (Exception e) {
            return new OrchestratorResponse(false, "Neuspeh pri kreiranju u Oracle/Django servisu: " + e.getMessage(), Map.of());
        }

        if (djangoResp.getStatusCode() == HttpStatus.CREATED || djangoResp.getStatusCode() == HttpStatus.OK) {
            DjangoVoziloDTO created = djangoResp.getBody();
            if (created == null) {
                return new OrchestratorResponse(false, "Django servis vratio je prazan odgovor.", Map.of());
            }

            // 2) try create in Neo4j
            try {
                Vozilo neo = voziloRepository.createVozilo(
                        created.getMarka(),
                        created.getModel(),
                        //created.getRegistracija(),
                        req.getRegistracija(), //normalno string polje iz requesta
                        created.getKapacitet(),
                        created.getStatus()
                );
                return new OrchestratorResponse(true, "Vozilo uspešno kreirano u oba servisa.",
                        Map.of("djangoId", created.getId(), "neo4jId", neo.getId()));
            } catch (Exception neoEx) {
                // rollback: delete created in Django
                try {
                    String deleteUrl = djangoBaseUrl + "vozila/delete/" + created.getId() + "/";
                    restTemplate.delete(deleteUrl);
                } catch (Exception delEx) {
                    return new OrchestratorResponse(false,
                            "Neo4j insert nije uspeo, rollback u Oracle nije uspeo. Manualna intervencija potrebna. Neo4j error: "
                                    + neoEx.getMessage() + " ; rollback error: " + delEx.getMessage(),
                            Map.of("djangoId", created.getId()));
                }
                return new OrchestratorResponse(false,
                        "Neo4j insert nije uspeo. Kreirani zapis u Django je obrisan (rollback). Greška: " + neoEx.getMessage(),
                        Map.of("djangoId", created.getId()));
            }

        } else {
            return new OrchestratorResponse(false, "Django servis vratio status: " + djangoResp.getStatusCode(), Map.of("status", djangoResp.getStatusCode()));
        }
    }
    public OrchestratorResponse changeStatusAndSync(Long djangoId, Long rsId, String newStatus) {
        log.info("=== CHANGE STATUS REQUEST ===");
        log.info("Django ID: {}, RS ID: {}, New Status: {}", djangoId, rsId, newStatus);
        log.info("Django Base URL: {}", djangoBaseUrl);

        // 1) Get current status
        String getUrl = djangoBaseUrl + "/api/vozila/" + djangoId + "/";
        log.info("GET URL: {}", getUrl);

        try {
            ResponseEntity<DjangoVoziloDTO> resp = restTemplate.getForEntity(getUrl, DjangoVoziloDTO.class);
            log.info("GET Response Status: {}", resp.getStatusCode());
            log.info("GET Response Body: {}", resp.getBody());

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                return new OrchestratorResponse(false,
                        "Ne mogu dohvatiti vozilo iz Django servisa (id=" + djangoId + ")",
                        Map.of("status", resp.getStatusCode()));
            }

            DjangoVoziloDTO existing = resp.getBody();
            String prevStatus = existing.getStatus();
            log.info("Previous Status: {}", prevStatus);

            // 2) Update Django
            String putUrl = djangoBaseUrl + "/api/vozila/update/" + djangoId + "/";
            log.info("PUT URL: {}", putUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            //headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            //String newnew = "\""+ newStatus +"\"";
            String jsonBody = "{ \"status\": \"" + newStatus + "\" }";
            Map<String, String> putPayload = Map.of("status", newStatus);
            //Map<String, String> putPayload = Map.of("\"status\"", newnew);
            //String putPayload = "\"status\" : " + newnew;
            log.info("PUT Payload: {}", putPayload);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            //HttpEntity<Map<String, String>> entity = new HttpEntity<>(putPayload, headers);

            ResponseEntity<String> putResp = restTemplate.exchange(
                    putUrl,
                    HttpMethod.PUT,
                    entity,
                    String.class
            );

            log.info("PUT Response Status: {}", putResp.getStatusCode());
            log.info("PUT Response Body: {}", putResp.getBody());

            if (!putResp.getStatusCode().is2xxSuccessful()) {
                return new OrchestratorResponse(false,
                        "Django nije prihvatio update statusa: " + putResp.getStatusCode(),
                        Map.of("response", putResp.getBody()));
            }

            // 3) Update Neo4j
            Vozilo updatedNeo = voziloRepository.updateStatus(rsId, newStatus);
            log.info("Neo4j updated successfully: {}", updatedNeo.getId());

            return new OrchestratorResponse(true,
                    "Status uspešno sinhronizovan u oba servisa.",
                    Map.of("djangoId", djangoId, "neo4jId", updatedNeo.getId(), "newStatus", newStatus));

        } catch (HttpClientErrorException e) {
            log.error("HTTP Client Error: {}", e.getMessage());
            log.error("Status Code: {}", e.getStatusCode());
            log.error("Response Body: {}", e.getResponseBodyAsString());
            return new OrchestratorResponse(false,
                    "HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(),
                    Map.of());
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            e.printStackTrace();
            return new OrchestratorResponse(false,
                    "Exception: " + e.getMessage(),
                    Map.of());
        }
    }

    // --- 2) Change status and sync ------------------------------------------------
    /*public OrchestratorResponse changeStatusAndSync(Long djangoId,Long rsId, String newStatus) {
        // 1) Get current status from Django to be able to rollback
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String getUrl = djangoBaseUrl + "/api/vozila/" + djangoId + "/";
        DjangoVoziloDTO existing;
        try {
            ResponseEntity<DjangoVoziloDTO> resp = restTemplate.getForEntity(getUrl, DjangoVoziloDTO.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                return new OrchestratorResponse(false, "Ne mogu dohvatiti vozilo iz Django servisa (id=" + djangoId + ")", Map.of("status", resp.getStatusCode()));
            }
            existing = resp.getBody();
        } catch (Exception e) {
            return new OrchestratorResponse(false, "Greška pri dohvatu iz Django servisa: " + e.getMessage(), Map.of());
        }

        String prevStatus = existing.getStatus();

        // 2) Update Django first (sačuvaj pređašnji status za rollback)
        String patchUrl = djangoBaseUrl + "api/vozila/update/" + djangoId + "/"; // prilagodi endpoint
        Map<String,String> patchPayload = Map.of("status", newStatus);
        try {
            HttpEntity<Map<String,String>> ent = new HttpEntity<>(patchPayload);
            ResponseEntity<Void> patchResp = restTemplate.exchange(patchUrl, HttpMethod.PUT, ent, Void.class);
            if (!patchResp.getStatusCode().is2xxSuccessful()) {
                return new OrchestratorResponse(false, "Django nije prihvatio update statusa: " + patchResp.getStatusCode(), Map.of());
            }
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            return new OrchestratorResponse(false, "Greška pri update-u u Django servisu: " + responseBody, Map.of());
        } catch (Exception e) {
            return new OrchestratorResponse(false, "Greška pri update-u u Django servisu: " + e.getMessage(), Map.of());
        }

        // 3) Update Neo4j
        try {
            //Vozilo nadjeno = voziloRepository.findByMarkaModel(resp.get);
            Vozilo updatedNeo = voziloRepository.updateStatus(rsId, newStatus); //updateStatusByRegistracija(registracija, newStatus);
            return new OrchestratorResponse(true, "Status uspešno sinhronizovan u oba servisa.",
                    Map.of("djangoId", djangoId, "neo4jId", updatedNeo.getId(), "newStatus", newStatus));
        } catch (Exception neoEx) {
            // rollback: vratiti status u Django na prevStatus
            try {
                String rollbackUrl = djangoBaseUrl + "api/vozila/update/" + djangoId + "/";
                Map<String,String> rollbackPayload = Map.of("status", prevStatus);
                HttpEntity<Map<String,String>> ent = new HttpEntity<>(rollbackPayload);
                restTemplate.exchange(rollbackUrl, HttpMethod.PUT, ent, Void.class);
            } catch (Exception rbEx) {
                return new OrchestratorResponse(false,
                        "Neo4j update nije uspeo, a rollback u Django nije uspeo. Manualna intervencija potrebna. Neo4j err: "
                                + neoEx.getMessage() + " ; rollback err: " + rbEx.getMessage(),
                        Map.of("djangoId", djangoId));
            }

            return new OrchestratorResponse(false,
                    "Neo4j update nije uspeo. Status vraćen u Django (rollback). Greška: " + neoEx.getMessage(),
                    Map.of("djangoId", djangoId));
        }
    }*/
}
