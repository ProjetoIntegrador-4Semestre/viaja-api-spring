package com.senac.viajaapi;

import java.util.List;

record GooglePlacesResponse(List<GooglePlace> places) {
}

record GooglePlace(
        String id,
        GoogleText displayName,
        String formattedAddress,
        GoogleLocation location,
        String primaryType,
        List<String> types,
        Double rating,
        Integer userRatingCount,
        String priceLevel,
        GoogleOpeningHours regularOpeningHours,
        String nationalPhoneNumber,
        String websiteUri,
        String googleMapsUri,
        GoogleText editorialSummary,
        List<GooglePhoto> photos) {
}

record GoogleText(String text, String languageCode) {
}

record GoogleLocation(Double latitude, Double longitude) {
}

record GoogleOpeningHours(Boolean openNow, List<String> weekdayDescriptions) {
}

record GooglePhoto(String name, Integer widthPx, Integer heightPx) {
}

record GooglePhotoMedia(String photoUri) {
}
