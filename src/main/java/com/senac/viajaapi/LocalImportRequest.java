package com.senac.viajaapi;

public record LocalImportRequest(String id) {

    public LocalImportRequest {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Place id is required");
        id = id.trim();
    }
}
