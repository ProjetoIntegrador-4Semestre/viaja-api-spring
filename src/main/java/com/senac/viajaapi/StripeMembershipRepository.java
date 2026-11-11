package com.senac.viajaapi;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StripeMembershipRepository extends JpaRepository<StripeMembership, Long> {
    @Query("select s.userId from StripeMembership s where s.reference = :reference")
    Optional<Long> findUserIdByReference(String reference);

    @Query("select s.userId from StripeMembership s where s.subscriptionId = :subscriptionId")
    Optional<Long> findUserIdBySubscriptionId(String subscriptionId);
}
