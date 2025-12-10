package com.example.demo.controller;

import com.example.demo.dto.CreateVoziloRequest;
import com.example.demo.dto.OrchestratorResponse;
import com.example.demo.service.OrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orchestrator")
@RequiredArgsConstructor
public class OrchestratorController {

    @Autowired
    private OrchestratorService orchestratorService;

    @PostMapping("/vozilo")
    public ResponseEntity<?> createAndRegisterVozilo(@RequestBody CreateVoziloRequest req) {
        OrchestratorResponse resp = orchestratorService.createAndRegisterVozilo(req);
        if (resp.isSuccess()) return ResponseEntity.ok(resp);
        return ResponseEntity.badRequest().body(resp);
    }

    @PatchMapping("/vozilo/{djangoId}/status")
    public ResponseEntity<?> changeStatus(@PathVariable Long djangoId,
                                          @RequestParam Long rsId,
                                          @RequestParam String newStatus) {
        OrchestratorResponse resp = orchestratorService.changeStatusAndSync(djangoId, rsId, newStatus);
        if (resp.isSuccess()) return ResponseEntity.ok(resp);
        return ResponseEntity.badRequest().body(resp);
    }
}
