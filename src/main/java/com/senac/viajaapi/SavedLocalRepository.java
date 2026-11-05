package com.senac.viajaapi;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedLocalRepository extends JpaRepository<SavedLocal, Long> {

    Page<SavedLocal> findByUserId(Long userId, Pageable pageable);

    boolean existsByUserIdAndLocalId(Long userId, Long localId);

    java.util.Optional<SavedLocal> findByUserIdAndLocalId(Long userId, Long localId);

    @org.springframework.transaction.annotation.Transactional
    void deleteByUserIdAndLocalId(Long userId, Long localId);
}
