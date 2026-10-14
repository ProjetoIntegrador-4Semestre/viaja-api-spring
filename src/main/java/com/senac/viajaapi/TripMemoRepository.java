package com.senac.viajaapi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripMemoRepository extends JpaRepository<TripMemo, Long> {

    @Query("select m from TripMemo m where m.tripId = ?1 and m.localId = ?2")
    List<TripMemo> findByTripIdAndLocalId(Long tripId, Long localId);
}
