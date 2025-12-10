package com.example.demo.dto;

import java.util.Map;

public class OrchestratorResponse {
    private boolean success;
    private String message;
    private Map<String, Object> details;

    public OrchestratorResponse() {}
    public OrchestratorResponse(boolean success, String message, Map<String, Object> details) {
        this.success = success; this.message = message; this.details = details;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}
