package com.senac.viajaapi;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class StripeEvent {
    @Id
    String id;

    protected StripeEvent() {
    }

    StripeEvent(String id) {
        this.id = id;
    }
}
