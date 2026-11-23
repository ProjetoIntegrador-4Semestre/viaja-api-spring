package com.senac.viajaapi;

import java.util.Locale;
import java.util.Set;

public record RouteRequest(MapCoordinate origin, MapCoordinate destination, String travelMode) {

    private static final Set<String> MODES = Set.of("DRIVE", "WALK", "BICYCLE", "TRANSIT");

    public RouteRequest {
        if (origin == null || destination == null) throw new IllegalArgumentException("Route origin and destination are required");
        travelMode = travelMode == null || travelMode.isBlank() ? "DRIVE" : travelMode.trim().toUpperCase(Locale.ROOT);
        if (!MODES.contains(travelMode)) throw new IllegalArgumentException("Unsupported travel mode: " + travelMode);
    }
}
