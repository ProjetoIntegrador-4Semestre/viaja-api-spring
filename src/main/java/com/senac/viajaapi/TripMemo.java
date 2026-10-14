package com.senac.viajaapi;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Table(name = "trip_memos")
@Entity
public class TripMemo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_id")
    private Long tripId;

    @Column(name = "local_id")
    private Long localId;

    @ManyToOne(optional = false)
    @JoinColumns({
            @JoinColumn(name = "trip_id", referencedColumnName = "trip_id", insertable = false, updatable = false),
            @JoinColumn(name = "local_id", referencedColumnName = "local_id", insertable = false, updatable = false)
    })
    private TripLocal tripLocal;

    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public TripMemo() {
    }

    public TripMemo(Long tripId, Long localId, String note, LocalDateTime createdAt) {
        this.tripId = tripId;
        this.localId = localId;
        this.note = note;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }
    public Long getLocalId() { return localId; }
    public void setLocalId(Long localId) { this.localId = localId; }
    public TripLocal getTripLocal() { return tripLocal; }
    public void setTripLocal(TripLocal tripLocal) { this.tripLocal = tripLocal; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TripMemo tripMemo = (TripMemo) o;
        return id.equals(tripMemo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TripMemo{id=" + id + ", tripId=" + tripId + ", localId=" + localId + "}";
    }
}
