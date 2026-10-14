package com.senac.viajaapi;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class UserInterestId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "interest_id")
    private Long interestId;

    public UserInterestId() {
    }

    public UserInterestId(Long userId, Long interestId) {
        this.userId = userId;
        this.interestId = interestId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getInterestId() { return interestId; }
    public void setInterestId(Long interestId) { this.interestId = interestId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInterestId that = (UserInterestId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(interestId, that.interestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, interestId);
    }
}
