package com.senac.viajaapi;

import java.util.List;

record GoogleRoutesResponse(List<GoogleRoute> routes) {
}

record GoogleRoute(Integer distanceMeters, String duration, GoogleRoutePolyline polyline) {
}

record GoogleRoutePolyline(String encodedPolyline) {
}

record GoogleGeocodeResults(List<GoogleGeocodeResult> results) {
}

record GoogleGeocodeResult(String placeId, String formattedAddress, GoogleLocation location) {
}
