package com.senac.viajaapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocalImportServiceTest {

    @Mock
    private ProviderRouter providerRouter;

    @Mock
    private LocalRepository repository;

    @Test
    void reusesPreviouslyImportedProviderPlace() {
        PlaceResponse external = new PlaceResponse();
        external.setSource("google");
        external.setSourceId("source-id");
        Local existing = new Local(null, "museum", "Museum", null, null, null, null, null, null,
                null, null, null, null, "google", "source-id", LocalDateTime.now(), LocalDateTime.now());
        existing.setId(12L);
        existing.setGoogleMapsUri("https://maps.google.com/example");
        existing.setPhotoName("places/source-id/photos/photo-id");
        existing.setPriceHint("$$");
        when(providerRouter.getById("google:source-id")).thenReturn(Optional.of(external));
        when(repository.findBySourceAndSourceId(LocalSource.GOOGLE, "source-id")).thenReturn(Optional.of(existing));

        PlaceResponse result = new LocalImportService(providerRouter, repository, new LocalResponseMapper())
                .importPlace("google:source-id");

        assertThat(result.getId()).isEqualTo("12");
        assertThat(result.getSource()).isEqualTo("google");
        assertThat(result.getCategories()).isEqualTo(List.of("museum"));
        assertThat(result.getGoogleMapsUri()).isEqualTo("https://maps.google.com/example");
        assertThat(result.getPhotoName()).isEqualTo("places/source-id/photos/photo-id");
        assertThat(result.getPriceHint()).isEqualTo("$$");
    }
}
