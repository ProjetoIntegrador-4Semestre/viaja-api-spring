package com.senac.viajaapi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripLocalRepository extends JpaRepository<TripLocal, TripLocalId> {

    List<TripLocal> findById_TripIdOrderByDayAscOrderIndexAsc(Long tripId);
}
