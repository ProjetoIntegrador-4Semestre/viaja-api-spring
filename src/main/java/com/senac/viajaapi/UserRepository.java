package com.senac.viajaapi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from User u where u.id = :id")
    Optional<User> lockById(Long id);

    Optional<User> findByEmail(String email);

    org.springframework.data.domain.Page<User> findById(Long id, org.springframework.data.domain.Pageable pageable);

    boolean existsByEmail(String email);
}
