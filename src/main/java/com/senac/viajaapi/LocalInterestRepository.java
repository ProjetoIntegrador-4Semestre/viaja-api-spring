package com.senac.viajaapi;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocalInterestRepository extends JpaRepository<LocalInterest, LocalInterestId> {

    Page<LocalInterest> findById_LocalId(Long localId, Pageable pageable);

    boolean existsByLocalIdAndInterestId(Long localId, Long interestId);

    void deleteByLocalIdAndInterestId(Long localId, Long interestId);
}
