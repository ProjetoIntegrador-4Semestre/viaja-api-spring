package com.senac.viajaapi;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Table(name = "local_interests")
@Entity
public class LocalInterest {

    @EmbeddedId
    private LocalInterestId id;

    public LocalInterest() {
    }

    public LocalInterest(Long localId, Long interestId) {
        this.id = new LocalInterestId(localId, interestId);
    }

    public LocalInterestId getId() { return id; }
    public void setId(LocalInterestId id) { this.id = id; }
    public Long getLocalId() { return id == null ? null : id.getLocalId(); }
    public void setLocalId(Long localId) { if (id == null) id = new LocalInterestId(); id.setLocalId(localId); }
    public Long getInterestId() { return id == null ? null : id.getInterestId(); }
    public void setInterestId(Long interestId) { if (id == null) id = new LocalInterestId(); id.setInterestId(interestId); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalInterest that = (LocalInterest) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "LocalInterest{localId=" + getLocalId() + ", interestId=" + getInterestId() + "}";
    }
}
