package com.senac.viajaapi;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class TripLocalId implements Serializable {

    @Column(name = "trip_id")
    private Long tripId;

    @Column(name = "local_id")
    private Long localId;

    public TripLocalId() {
    }

    public TripLocalId(Long tripId, Long localId) {
        this.tripId = tripId;
        this.localId = localId;
    }

    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }
    public Long getLocalId() { return localId; }
    public void setLocalId(Long localId) { this.localId = localId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TripLocalId that = (TripLocalId) o;
        return Objects.equals(tripId, that.tripId) && Objects.equals(localId, that.localId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tripId, localId);
    }
}
