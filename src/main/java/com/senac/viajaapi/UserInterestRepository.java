package com.senac.viajaapi;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, UserInterestId> {

    Page<UserInterest> findById_UserId(Long userId, Pageable pageable);

    boolean existsByUserIdAndInterestId(Long userId, Long interestId);

    void deleteByUserIdAndInterestId(Long userId, Long interestId);
}
