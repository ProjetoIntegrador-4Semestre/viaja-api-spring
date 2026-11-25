package com.senac.viajaapi;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Component
public class GooglePlacesClient {

    private static final String SEARCH_FIELDS = "places.id,places.displayName,places.formattedAddress,places.location,places.primaryType,places.types,places.rating,places.userRatingCount,places.priceLevel,places.regularOpeningHours,places.nationalPhoneNumber,places.websiteUri,places.googleMapsUri,places.editorialSummary,places.photos";
    private static final String DETAILS_FIELDS = "id,displayName,formattedAddress,location,primaryType,types,rating,userRatingCount,priceLevel,regularOpeningHours,nationalPhoneNumber,websiteUri,googleMapsUri,editorialSummary,photos";

    private final String apiKey;
    private final RestClient client;

    public GooglePlacesClient(
            @Value("${google.maps.server-api-key:}") String apiKey,
            @Value("${google.maps.places-base-url:https://places.googleapis.com}") String baseUrl) {
        this.apiKey = apiKey;
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build());
        factory.setReadTimeout(Duration.ofSeconds(10));
        client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public List<GooglePlace> searchText(String query) {
        if (query == null || query.isBlank()) return List.of();
        Map<String, Object> body = Map.of(
                "textQuery", query,
                "languageCode", "pt-BR",
                "regionCode", "BR",
                "pageSize", 20
        );
        GooglePlacesResponse response = post("/v1/places:searchText", SEARCH_FIELDS, body);
        return response == null || response.places() == null ? List.of() : response.places();
    }

    public List<GooglePlace> searchNearby(NearbySearchRequest request) {
        Map<String, Object> center = Map.of("latitude", request.latitude(), "longitude", request.longitude());
        Map<String, Object> circle = Map.of("center", center, "radius", request.radiusMeters());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("languageCode", "pt-BR");
        body.put("regionCode", "BR");
        body.put("maxResultCount", 20);
        body.put("rankPreference", "POPULARITY");
        body.put("locationRestriction", Map.of("circle", circle));
        if (request.type() != null) body.put("includedTypes", List.of(request.type()));
        GooglePlacesResponse response = post("/v1/places:searchNearby", SEARCH_FIELDS, body);
        return response == null || response.places() == null ? List.of() : response.places();
    }

    public Optional<GooglePlace> getPlace(String placeId) {
        requireConfiguration();
        try {
            GooglePlace place = client.get()
                    .uri(uriBuilder -> uriBuilder.path("/v1/places/")
                            .pathSegment(placeId)
                            .queryParam("languageCode", "pt-BR")
                            .queryParam("regionCode", "BR")
                            .build())
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", DETAILS_FIELDS)
                    .retrieve()
                    .body(GooglePlace.class);
            return Optional.ofNullable(place);
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound exception) {
            return Optional.empty();
        } catch (RestClientException exception) {
            throw unavailable(exception);
        }
    }

    public Optional<String> getPhotoUri(String photoName, int maxWidthPx) {
        requireConfiguration();
        if (photoName == null || !photoName.matches("places/[^/]+/photos/[^/]+")) {
            throw new IllegalArgumentException("Invalid Google photo name");
        }
        try {
            GooglePhotoMedia media = client.get()
                    .uri(uriBuilder -> uriBuilder.path("/v1/")
                            .path(photoName)
                            .path("/media")
                            .queryParam("maxWidthPx", maxWidthPx)
                            .queryParam("skipHttpRedirect", true)
                            .build())
                    .header("X-Goog-Api-Key", apiKey)
                    .retrieve()
                    .body(GooglePhotoMedia.class);
            return media == null ? Optional.empty() : Optional.ofNullable(media.photoUri());
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound exception) {
            return Optional.empty();
        } catch (RestClientException exception) {
            throw unavailable(exception);
        }
    }

    private GooglePlacesResponse post(String path, String fields, Map<String, Object> body) {
        requireConfiguration();
        try {
            return client.post()
                    .uri(path)
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", fields)
                    .body(body)
                    .retrieve()
                    .body(GooglePlacesResponse.class);
        } catch (RestClientException exception) {
            throw unavailable(exception);
        }
    }

    private void requireConfiguration() {
        if (apiKey == null || apiKey.isBlank()) throw new MapsConfigurationException();
    }

    private ResponseStatusException unavailable(RestClientException exception) {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Google Places is temporarily unavailable", exception);
    }
}
