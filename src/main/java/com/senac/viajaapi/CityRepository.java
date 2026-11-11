package com.senac.viajaapi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    java.util.Optional<City> findFirstByNameAndStateAndCountryOrderByIdAsc(String name, String state, String country);

}
