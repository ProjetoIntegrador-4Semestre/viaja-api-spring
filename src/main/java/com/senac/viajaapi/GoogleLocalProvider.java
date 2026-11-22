package com.senac.viajaapi;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class GoogleLocalProvider implements LocalProvider {

    private final GooglePlacesClient client;
    private final GooglePlaceMapper mapper;

    public GoogleLocalProvider(GooglePlacesClient client, GooglePlaceMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public LocalSource source() {
        return LocalSource.GOOGLE;
    }

    @Override
    public List<PlaceResponse> search(LocalSearchRequest request) {
        return client.searchText(request.query()).stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<PlaceResponse> searchNearby(NearbySearchRequest request) {
        return client.searchNearby(request).stream().map(mapper::toResponse).toList();
    }

    @Override
    public Optional<PlaceResponse> getById(String id) {
        return client.getPlace(id).map(mapper::toResponse);
    }

    @Override
    public Optional<URI> resolvePhoto(String photoName, int maxWidthPx) {
        return client.getPhotoUri(photoName, maxWidthPx).map(URI::create);
    }
}
