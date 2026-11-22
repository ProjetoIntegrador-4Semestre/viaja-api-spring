package com.senac.viajaapi;

public record NearbySearchRequest(double latitude, double longitude, double radiusMeters, String type) {

    public NearbySearchRequest {
        if (latitude < -90 || latitude > 90) throw new IllegalArgumentException("Latitude must be between -90 and 90");
        if (longitude < -180 || longitude > 180) throw new IllegalArgumentException("Longitude must be between -180 and 180");
        if (radiusMeters <= 0 || radiusMeters > 50000) throw new IllegalArgumentException("Radius must be between 1 and 50000 meters");
        type = type == null || type.isBlank() ? null : type.trim();
    }
}
