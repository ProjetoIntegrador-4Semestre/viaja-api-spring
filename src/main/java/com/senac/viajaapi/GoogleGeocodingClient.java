package com.senac.viajaapi;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class GoogleGeocodingClient {

    private static final String FIELDS = "results.placeId,results.formattedAddress,results.location";

    private final String apiKey;
    private final RestClient client;

    public GoogleGeocodingClient(
            @Value("${google.maps.server-api-key:}") String apiKey,
            @Value("${google.maps.geocoding-base-url:https://geocode.googleapis.com}") String baseUrl) {
        this.apiKey = apiKey;
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build());
        factory.setReadTimeout(Duration.ofSeconds(10));
        client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public List<GeocodeResponse> geocode(String address) {
        if (address == null || address.isBlank()) throw new IllegalArgumentException("Address is required");
        return request("/v4/geocode/address/", address.trim());
    }

    public List<GeocodeResponse> reverse(MapCoordinate coordinate) {
        return request("/v4/geocode/location/", coordinate.latitude() + "," + coordinate.longitude());
    }

    private List<GeocodeResponse> request(String path, String value) {
        requireConfiguration();
        try {
            GoogleGeocodeResults response = client.get()
                    .uri(uriBuilder -> uriBuilder.path(path)
                            .pathSegment(value)
                            .queryParam("languageCode", "pt-BR")
                            .queryParam("regionCode", "BR")
                            .build())
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", FIELDS)
                    .retrieve()
                    .body(GoogleGeocodeResults.class);
            if (response == null || response.results() == null) return List.of();
            return response.results().stream()
                    .filter(result -> result.location() != null)
                    .map(result -> new GeocodeResponse(
                            result.placeId(),
                            result.formattedAddress(),
                            result.location().latitude(),
                            result.location().longitude()
                    ))
                    .toList();
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Google Geocoding is temporarily unavailable", exception);
        }
    }

    private void requireConfiguration() {
        if (apiKey == null || apiKey.isBlank()) throw new MapsConfigurationException();
    }
}
