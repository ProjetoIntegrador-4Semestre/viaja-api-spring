package com.senac.viajaapi;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class LocalImportService {

    private final ProviderRouter providerRouter;
    private final LocalRepository repository;
    private final LocalResponseMapper mapper;

    public LocalImportService(ProviderRouter providerRouter, LocalRepository repository, LocalResponseMapper mapper) {
        this.providerRouter = providerRouter;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public PlaceResponse importPlace(String id) {
        PlaceResponse place = providerRouter.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place was not found"));
        LocalSource source = LocalSource.fromValue(place.getSource());
        String sourceId = place.getSourceId();
        if (sourceId == null || sourceId.isBlank()) throw new IllegalArgumentException("Provider place id is required");
        return repository.findBySourceAndSourceId(source, sourceId)
                .map(mapper::toResponse)
                .orElseGet(() -> mapper.toResponse(repository.save(toLocal(place, source, sourceId))));
    }

    private Local toLocal(PlaceResponse place, LocalSource source, String sourceId) {
        LocalDateTime now = LocalDateTime.now();
        Local local = new Local();
        local.setType(place.getCategories() == null || place.getCategories().isEmpty() ? null : place.getCategories().get(0));
        local.setName(place.getName());
        local.setAddress(place.getAddress());
        local.setLat(place.getLat() == null ? null : BigDecimal.valueOf(place.getLat()));
        local.setLng(place.getLng() == null ? null : BigDecimal.valueOf(place.getLng()));
        local.setPrice(place.getPrice());
        local.setRating(place.getRating());
        local.setRatingCount(place.getRatingCount());
        local.setOpeningHours(place.getOpeningHours());
        local.setDescription(place.getDescription());
        local.setPhone(place.getPhone());
        local.setWebsite(place.getWebsite());
        local.setSource(source);
        local.setSourceId(sourceId);
        local.setCreatedAt(now);
        local.setUpdatedAt(now);
        return local;
    }
}
