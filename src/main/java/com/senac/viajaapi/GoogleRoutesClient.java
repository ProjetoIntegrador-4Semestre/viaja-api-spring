package com.senac.viajaapi;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class GoogleRoutesClient {

    private final String apiKey;
    private final RestClient client;

    public GoogleRoutesClient(
            @Value("${google.maps.server-api-key:}") String apiKey,
            @Value("${google.maps.routes-base-url:https://routes.googleapis.com}") String baseUrl) {
        this.apiKey = apiKey;
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build());
        factory.setReadTimeout(Duration.ofSeconds(12));
        client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public RouteResponse compute(RouteRequest request) {
        requireConfiguration();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("origin", waypoint(request.origin()));
        body.put("destination", waypoint(request.destination()));
        body.put("travelMode", request.travelMode());
        if ("DRIVE".equals(request.travelMode())) body.put("routingPreference", "TRAFFIC_AWARE");
        body.put("computeAlternativeRoutes", false);
        body.put("languageCode", "pt-BR");
        body.put("units", "METRIC");
        try {
            GoogleRoutesResponse response = client.post()
                    .uri("/directions/v2:computeRoutes")
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", "routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline")
                    .body(body)
                    .retrieve()
                    .body(GoogleRoutesResponse.class);
            GoogleRoute route = response == null || response.routes() == null || response.routes().isEmpty()
                    ? null : response.routes().get(0);
            if (route == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No route was found");
            return new RouteResponse(
                    route.distanceMeters() == null ? 0 : route.distanceMeters(),
                    route.duration(),
                    route.polyline() == null ? null : route.polyline().encodedPolyline()
            );
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Google Routes is temporarily unavailable", exception);
        }
    }

    private Map<String, Object> waypoint(MapCoordinate coordinate) {
        return Map.of("location", Map.of("latLng", Map.of(
                "latitude", coordinate.latitude(),
                "longitude", coordinate.longitude()
        )));
    }

    private void requireConfiguration() {
        if (apiKey == null || apiKey.isBlank()) throw new MapsConfigurationException();
    }
}
