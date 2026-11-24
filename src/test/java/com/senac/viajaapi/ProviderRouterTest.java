package com.senac.viajaapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class ProviderRouterTest {

    @Test
    void routesRequestsToConfiguredProvider() {
        PlaceResponse place = new PlaceResponse();
        place.setId("1");
        LocalProvider provider = new LocalProvider() {
            @Override
            public LocalSource source() {
                return LocalSource.MOCK;
            }

            @Override
            public List<PlaceResponse> search(LocalSearchRequest request) {
                return List.of(place);
            }

            @Override
            public Optional<PlaceResponse> getById(String id) {
                return Optional.of(place);
            }
        };

        ProviderRouter router = new ProviderRouter(List.of(provider), "mock");

        assertThat(router.defaultSource()).isEqualTo(LocalSource.MOCK);
        assertThat(router.search(new LocalSearchRequest(null))).containsExactly(place);
        assertThat(router.getById("1")).contains(place);
    }

    @Test
    void routesQualifiedIdsToTheirSource() {
        PlaceResponse mockPlace = new PlaceResponse();
        mockPlace.setId("1");
        PlaceResponse googlePlace = new PlaceResponse();
        googlePlace.setId("google:abc");
        LocalProvider mock = provider(LocalSource.MOCK, mockPlace);
        LocalProvider google = provider(LocalSource.GOOGLE, googlePlace);
        ProviderRouter router = new ProviderRouter(List.of(mock, google), "google");

        assertThat(router.getById("1")).contains(mockPlace);
        assertThat(router.getById("google:abc")).contains(googlePlace);
    }

    @Test
    void routesGooglePhotoResourcesIndependentlyFromDefaultProvider() {
        LocalProvider mock = provider(LocalSource.MOCK, new PlaceResponse());
        LocalProvider google = new LocalProvider() {
            @Override
            public LocalSource source() {
                return LocalSource.GOOGLE;
            }

            @Override
            public List<PlaceResponse> search(LocalSearchRequest request) {
                return List.of();
            }

            @Override
            public Optional<PlaceResponse> getById(String id) {
                return Optional.empty();
            }

            @Override
            public Optional<URI> resolvePhoto(String photoName, int maxWidthPx) {
                return Optional.of(URI.create("https://example.com/photo"));
            }
        };
        ProviderRouter router = new ProviderRouter(List.of(mock, google), "mock");

        assertThat(router.resolvePhoto("places/abc/photos/def", 800))
                .contains(URI.create("https://example.com/photo"));
    }

    private LocalProvider provider(LocalSource source, PlaceResponse place) {
        return new LocalProvider() {
            @Override
            public LocalSource source() {
                return source;
            }

            @Override
            public List<PlaceResponse> search(LocalSearchRequest request) {
                return List.of(place);
            }

            @Override
            public Optional<PlaceResponse> getById(String id) {
                return Optional.of(place);
            }
        };
    }
}
