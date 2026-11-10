package com.senac.viajaapi;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripMemoRepository extends JpaRepository<TripMemo, Long> {

    @Query("select m from TripMemo m where m.tripId = ?1 and m.localId = ?2")
    List<TripMemo> findByTripIdAndLocalId(Long tripId, Long localId);
    @Query("select m from TripMemo m where m.tripId in (select t.id from Trip t where t.userId = :userId)")
    Page<TripMemo> findOwnedBy(Long userId, Pageable pageable);

    void deleteByTripId(Long tripId);
}
