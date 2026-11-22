package com.senac.viajaapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MockLocalProviderTest {

    @Mock
    private LocalRepository localRepository;

    @Mock
    private CityRepository cityRepository;

    private MockLocalProvider provider;
    private Local local;

    @BeforeEach
    void setUp() {
        provider = new MockLocalProvider(localRepository, cityRepository, new LocalResponseMapper());
        local = new Local(10L, "museum", "Museum of Art", "Central Avenue", BigDecimal.ONE,
                BigDecimal.TEN, 1000, 4.5f, 20, "09:00-18:00", "Modern art", "123",
                "https://example.com", "mock", null, LocalDateTime.now(), LocalDateTime.now());
        local.setId(5L);
    }

    @Test
    void searchesExistingCatalogByCity() {
        when(cityRepository.findAll()).thenReturn(List.of(new City("Sao Paulo", "SP", "Brazil", null)));
        cityRepository.findAll().get(0).setId(10L);
        when(localRepository.findAll()).thenReturn(List.of(local));

        List<PlaceResponse> result = provider.search(new LocalSearchRequest("sao paulo"));

        assertThat(result).singleElement().satisfies(place -> {
            assertThat(place.getId()).isEqualTo("5");
            assertThat(place.getSource()).isEqualTo("mock");
            assertThat(place.getPhone()).isEqualTo("123");
            assertThat(place.getOpeningHours()).isEqualTo("09:00-18:00");
        });
    }

    @Test
    void returnsExistingPlaceByNumericId() {
        when(localRepository.findById(5L)).thenReturn(Optional.of(local));

        assertThat(provider.getById("5")).get().extracting(PlaceResponse::getName).isEqualTo("Museum of Art");
        assertThat(provider.getById("google-id")).isEmpty();
    }
}
