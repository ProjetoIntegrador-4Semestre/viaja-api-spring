package com.senac.viajaapi;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class LocalResponseMapper {

    public PlaceResponse toResponse(Local local) {
        PlaceResponse response = new PlaceResponse(
                String.valueOf(local.getId()),
                local.getSource() == null ? null : local.getSource().value(),
                local.getSourceId(),
                local.getName(),
                local.getAddress(),
                local.getLat() == null ? null : local.getLat().doubleValue(),
                local.getLng() == null ? null : local.getLng().doubleValue(),
                local.getType() == null ? List.of() : List.of(local.getType()),
                local.getRating(),
                local.getRatingCount(),
                local.getPrice(),
                local.getPriceHint(),
                null,
                null,
                new ArrayList<>()
        );
        response.setDescription(local.getDescription());
        response.setWebsite(local.getWebsite());
        response.setPhone(local.getPhone());
        response.setOpeningHours(local.getOpeningHours());
        response.setGoogleMapsUri(local.getGoogleMapsUri());
        response.setPhotoName(local.getPhotoName());
        return response;
    }
}
