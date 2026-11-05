package com.senac.viajaapi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripLocalRepository extends JpaRepository<TripLocal, TripLocalId> {

    org.springframework.data.domain.Page<TripLocal> findById_TripIdIn(List<Long> tripIds, org.springframework.data.domain.Pageable pageable);

    List<TripLocal> findById_TripIdOrderByDayAscOrderIndexAsc(Long tripId);
    void deleteById_TripId(Long tripId);
}
