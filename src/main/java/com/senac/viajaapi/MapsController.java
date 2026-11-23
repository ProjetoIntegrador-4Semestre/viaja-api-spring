package com.senac.viajaapi;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/maps")
public class MapsController {

    private final GoogleRoutesClient routes;
    private final GoogleGeocodingClient geocoding;

    public MapsController(GoogleRoutesClient routes, GoogleGeocodingClient geocoding) {
        this.routes = routes;
        this.geocoding = geocoding;
    }

    @Operation(summary = "Calcular rota", description = "Calcula uma rota e devolve distância, duração e polyline codificada.")
    @PostMapping("/routes")
    public RouteResponse route(@RequestBody RouteRequest request) {
        return routes.compute(request);
    }

    @Operation(summary = "Geocodificar endereço", description = "Converte um endereço em coordenadas.")
    @GetMapping("/geocode")
    public List<GeocodeResponse> geocode(@RequestParam String address) {
        return geocoding.geocode(address);
    }

    @Operation(summary = "Geocodificação reversa", description = "Converte coordenadas em endereços próximos.")
    @GetMapping("/reverse-geocode")
    public List<GeocodeResponse> reverseGeocode(@RequestParam double lat, @RequestParam double lng) {
        return geocoding.reverse(new MapCoordinate(lat, lng));
    }
}
