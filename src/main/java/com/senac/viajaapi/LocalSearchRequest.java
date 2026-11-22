package com.senac.viajaapi;

public record LocalSearchRequest(String query) {

    public LocalSearchRequest {
        query = query == null ? null : query.trim();
    }

    public boolean hasQuery() {
        return query != null && !query.isBlank();
    }
}
