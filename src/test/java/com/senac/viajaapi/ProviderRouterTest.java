package com.senac.viajaapi;

import static org.assertj.core.api.Assertions.assertThat;

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
}
