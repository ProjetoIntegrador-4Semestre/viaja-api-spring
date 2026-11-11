package com.senac.viajaapi;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class StripeMembership {
    @Id
    Long userId;

    @Column(unique = true)
    String reference;

    @Column(unique = true)
    String subscriptionId;

    @Column(unique = true)
    String customerId;

    String status;
    Instant paidUntil;
    boolean cancelAtPeriodEnd;
}
