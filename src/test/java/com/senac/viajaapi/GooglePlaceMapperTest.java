package com.senac.viajaapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class GooglePlaceMapperTest {

    @Test
    void mapsGoogleDataWithoutTreatingPriceLevelAsMoney() {
        GooglePlace place = new GooglePlace(
                "google-place-id",
                new GoogleText("Museum", "en"),
                "Central Avenue",
                new GoogleLocation(-23.5, -46.6),
                "museum",
                List.of("museum", "tourist_attraction"),
                4.7,
                350,
                "PRICE_LEVEL_MODERATE",
                new GoogleOpeningHours(true, List.of("Monday: 9:00 AM – 6:00 PM")),
                "(11) 1234-5678",
                "https://example.com",
                "https://maps.google.com/example",
                new GoogleText("A modern art museum", "en"),
                List.of(new GooglePhoto("places/google-place-id/photos/photo-id", 1200, 800))
        );

        PlaceResponse result = new GooglePlaceMapper().toResponse(place);

        assertThat(result.getId()).isEqualTo("google:google-place-id");
        assertThat(result.getSource()).isEqualTo("google");
        assertThat(result.getPrice()).isNull();
        assertThat(result.getPriceHint()).isEqualTo("$$");
        assertThat(result.getOpenNow()).isTrue();
        assertThat(result.getPhotoName()).isEqualTo("places/google-place-id/photos/photo-id");
        assertThat(result.getCategories()).containsExactly("museum", "tourist_attraction");
    }
}
