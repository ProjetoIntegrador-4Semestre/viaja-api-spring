package com.senac.viajaapi;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class MockLocalProvider implements LocalProvider {

    private final LocalRepository localRepository;
    private final CityRepository cityRepository;
    private final LocalResponseMapper mapper;

    public MockLocalProvider(LocalRepository localRepository, CityRepository cityRepository, LocalResponseMapper mapper) {
        this.localRepository = localRepository;
        this.cityRepository = cityRepository;
        this.mapper = mapper;
    }

    @Override
    public LocalSource source() {
        return LocalSource.MOCK;
    }

    @Override
    public List<PlaceResponse> search(LocalSearchRequest request) {
        if (!request.hasQuery()) return localRepository.findAll().stream().map(mapper::toResponse).toList();

        String term = request.query().toLowerCase(Locale.ROOT);
        List<Long> cityIds = cityRepository.findAll().stream()
                .filter(city -> city.getName().toLowerCase(Locale.ROOT).contains(term))
                .map(City::getId)
                .toList();

        return localRepository.findAll().stream()
                .filter(local -> matches(local, term, cityIds))
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public Optional<PlaceResponse> getById(String id) {
        try {
            return localRepository.findById(Long.valueOf(id)).map(mapper::toResponse);
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private boolean matches(Local local, String term, List<Long> cityIds) {
        return local.getName().toLowerCase(Locale.ROOT).contains(term)
                || local.getAddress() != null && local.getAddress().toLowerCase(Locale.ROOT).contains(term)
                || cityIds.contains(local.getCityId());
    }
}
