package com.senac.viajaapi;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public interface LocalProvider {
    LocalSource source();

    List<PlaceResponse> search(LocalSearchRequest request);

    default List<PlaceResponse> searchNearby(NearbySearchRequest request) {
        return List.of();
    }

    Optional<PlaceResponse> getById(String id);

    default Optional<URI> resolvePhoto(String photoName, int maxWidthPx) {
        return Optional.empty();
    }
}
