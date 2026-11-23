package com.senac.viajaapi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LocalRepository extends JpaRepository<Local, Long> {
    boolean existsByCityIdAndNameIgnoreCase(Long cityId, String name);
    Optional<Local> findBySourceAndSourceId(LocalSource source, String sourceId);
}
