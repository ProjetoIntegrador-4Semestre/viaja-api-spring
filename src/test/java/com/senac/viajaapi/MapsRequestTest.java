package com.senac.viajaapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MapsRequestTest {

    @Test
    void normalizesSupportedTravelMode() {
        RouteRequest request = new RouteRequest(
                new MapCoordinate(-23.5, -46.6),
                new MapCoordinate(-23.6, -46.7),
                "walk"
        );

        assertThat(request.travelMode()).isEqualTo("WALK");
    }

    @Test
    void rejectsInvalidCoordinatesAndModes() {
        assertThatThrownBy(() -> new MapCoordinate(91, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RouteRequest(
                new MapCoordinate(0, 0),
                new MapCoordinate(1, 1),
                "FLY"
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reportsMissingServerConfigurationBeforeCallingGoogle() {
        GoogleRoutesClient client = new GoogleRoutesClient("", "https://routes.googleapis.com");
        RouteRequest request = new RouteRequest(
                new MapCoordinate(0, 0),
                new MapCoordinate(1, 1),
                "DRIVE"
        );

        assertThatThrownBy(() -> client.compute(request))
                .isInstanceOf(MapsConfigurationException.class);
    }
}
