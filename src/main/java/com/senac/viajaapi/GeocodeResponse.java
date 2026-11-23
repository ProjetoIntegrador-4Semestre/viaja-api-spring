package com.senac.viajaapi;

public record GeocodeResponse(String placeId, String formattedAddress, double latitude, double longitude) {
}
