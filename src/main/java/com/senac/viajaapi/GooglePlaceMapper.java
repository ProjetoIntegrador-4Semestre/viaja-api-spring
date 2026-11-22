package com.senac.viajaapi;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class GooglePlaceMapper {

    public PlaceResponse toResponse(GooglePlace place) {
        PlaceResponse response = new PlaceResponse(
                LocalSource.GOOGLE.value() + ":" + place.id(),
                LocalSource.GOOGLE.value(),
                place.id(),
                place.displayName() == null ? "" : place.displayName().text(),
                place.formattedAddress(),
                place.location() == null ? null : place.location().latitude(),
                place.location() == null ? null : place.location().longitude(),
                categories(place),
                place.rating() == null ? null : place.rating().floatValue(),
                place.userRatingCount(),
                null,
                priceHint(place.priceLevel()),
                null,
                place.regularOpeningHours() == null ? null : place.regularOpeningHours().openNow(),
                new ArrayList<>()
        );
        response.setDescription(place.editorialSummary() == null ? null : place.editorialSummary().text());
        response.setWebsite(place.websiteUri());
        response.setPhone(place.nationalPhoneNumber());
        response.setOpeningHours(openingHours(place.regularOpeningHours()));
        response.setGoogleMapsUri(place.googleMapsUri());
        if (place.photos() != null && !place.photos().isEmpty()) response.setPhotoName(place.photos().get(0).name());
        return response;
    }

    private List<String> categories(GooglePlace place) {
        if (place.types() != null && !place.types().isEmpty()) return place.types();
        return place.primaryType() == null ? List.of() : List.of(place.primaryType());
    }

    private String openingHours(GoogleOpeningHours hours) {
        if (hours == null || hours.weekdayDescriptions() == null) return null;
        return String.join("\n", hours.weekdayDescriptions());
    }

    private String priceHint(String priceLevel) {
        if (priceLevel == null) return null;
        return switch (priceLevel) {
            case "PRICE_LEVEL_FREE" -> "Free";
            case "PRICE_LEVEL_INEXPENSIVE" -> "$";
            case "PRICE_LEVEL_MODERATE" -> "$$";
            case "PRICE_LEVEL_EXPENSIVE" -> "$$$";
            case "PRICE_LEVEL_VERY_EXPENSIVE" -> "$$$$";
            default -> null;
        };
    }
}
