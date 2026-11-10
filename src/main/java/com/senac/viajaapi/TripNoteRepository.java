package com.senac.viajaapi;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripNoteRepository extends JpaRepository<TripNote, Long> {

    Page<TripNote> findByTripId(Long tripId, Pageable pageable);
    Optional<TripNote> findByIdAndTripId(Long id, Long tripId);
    void deleteByTripId(Long tripId);
}
