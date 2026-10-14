package com.senac.viajaapi;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class LocalInterestId implements Serializable {

    @Column(name = "local_id")
    private Long localId;

    @Column(name = "interest_id")
    private Long interestId;

    public LocalInterestId() {
    }

    public LocalInterestId(Long localId, Long interestId) {
        this.localId = localId;
        this.interestId = interestId;
    }

    public Long getLocalId() { return localId; }
    public void setLocalId(Long localId) { this.localId = localId; }
    public Long getInterestId() { return interestId; }
    public void setInterestId(Long interestId) { this.interestId = interestId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalInterestId that = (LocalInterestId) o;
        return Objects.equals(localId, that.localId) && Objects.equals(interestId, that.interestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localId, interestId);
    }
}
