package com.senac.viajaapi;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EmbeddedId;

@Entity
public class TripLocal {

    @EmbeddedId
    private TripLocalId id;

    private Integer orderIndex;

    @Column(name = "day_order")
    private Integer day;

    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public TripLocal() {
    }

    public TripLocal(Long tripId, Long localId, Integer orderIndex, Integer day, String note) {
        this.id = new TripLocalId(tripId, localId);
        this.orderIndex = orderIndex;
        this.day = day;
        this.note = note;
        this.createdAt = LocalDateTime.now();
    }

    public TripLocalId getId() { return id; }
    public void setId(TripLocalId id) { this.id = id; }
    public Long getTripId() { return id == null ? null : id.getTripId(); }
    public void setTripId(Long tripId) { if (id == null) id = new TripLocalId(); id.setTripId(tripId); }
    public Long getLocalId() { return id == null ? null : id.getLocalId(); }
    public void setLocalId(Long localId) { if (id == null) id = new TripLocalId(); id.setLocalId(localId); }
    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
    public Integer getDay() { return day; }
    public void setDay(Integer day) { this.day = day; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TripLocal tripLocal = (TripLocal) o;
        return id.equals(tripLocal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TripLocal{tripId=" + getTripId() + ", localId=" + getLocalId() + "}";
    }
}
