package com.senac.viajaapi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
public class BrazilCatalog implements CommandLineRunner {
    private final CityRepository cities;
    private final LocalRepository places;

    public BrazilCatalog(CityRepository cities, LocalRepository places) {
        this.cities = cities;
        this.places = places;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        Map<String, City> destinations = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        try (var reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("catalog/brazil.txt").getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] entry = line.split("\\|", -1);
                if (entry.length != 7) throw new IllegalStateException("Invalid Brazil catalog row");
                String cityName = entry[0];
                String state = entry[1];
                City city = destinations.computeIfAbsent(cityName + "/" + state, key ->
                        cities.findFirstByNameAndStateAndCountryOrderByIdAsc(cityName, state, "Brasil")
                                .orElseGet(() -> cities.save(new City(cityName, state, "Brasil", null))));
                String name = entry[2];
                if (places.existsByCityIdAndNameIgnoreCase(city.getId(), name)) continue;
                places.save(new Local(city.getId(), "attraction", name, entry[3],
                        new BigDecimal(entry[4]), new BigDecimal(entry[5]), null, null, null,
                        null, entry[6], null, null, "catalog", null, now, now));
            }
        }
    }
}
