package com.senac.viajaapi;

import java.net.URI;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProviderRouter {

    private final Map<LocalSource, LocalProvider> providers;
    private final LocalSource defaultSource;

    public ProviderRouter(List<LocalProvider> providers,
            @Value("${viaja.locals.provider:mock}") String defaultSource) {
        this.providers = new EnumMap<>(LocalSource.class);
        providers.forEach(provider -> this.providers.put(provider.source(), provider));
        this.defaultSource = LocalSource.fromValue(defaultSource);
        provider(this.defaultSource);
    }

    public List<PlaceResponse> search(LocalSearchRequest request) {
        return provider(defaultSource).search(request);
    }

    public Optional<PlaceResponse> getById(String id) {
        return provider(defaultSource).getById(id);
    }

    public List<PlaceResponse> searchNearby(NearbySearchRequest request) {
        return provider(defaultSource).searchNearby(request);
    }

    public Optional<URI> resolvePhoto(String photoName, int maxWidthPx) {
        return provider(defaultSource).resolvePhoto(photoName, maxWidthPx);
    }

    public LocalProvider provider(LocalSource source) {
        LocalProvider provider = providers.get(source);
        if (provider == null) throw new IllegalStateException("Local provider is not available: " + source.value());
        return provider;
    }

    public LocalSource defaultSource() {
        return defaultSource;
    }
}
