package com.senac.viajaapi;

import java.util.List;
import java.util.Optional;

public interface LocalProvider {
    LocalSource source();

    List<PlaceResponse> search(LocalSearchRequest request);

    Optional<PlaceResponse> getById(String id);
}
