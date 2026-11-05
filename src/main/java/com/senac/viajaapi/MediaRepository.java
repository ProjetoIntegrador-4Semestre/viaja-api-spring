package com.senac.viajaapi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    List<Media> findByEntityTypeAndEntityId(String entityType, Long entityId);
    void deleteByEntityTypeAndEntityId(String entityType, Long entityId);
}
