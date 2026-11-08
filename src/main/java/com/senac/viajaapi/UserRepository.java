package com.senac.viajaapi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    org.springframework.data.domain.Page<User> findById(Long id, org.springframework.data.domain.Pageable pageable);

    boolean existsByEmail(String email);
}
