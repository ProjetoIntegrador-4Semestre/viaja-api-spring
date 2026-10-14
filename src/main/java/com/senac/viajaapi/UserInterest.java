package com.senac.viajaapi;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Table(name = "user_interests")
@Entity
public class UserInterest {

    @EmbeddedId
    private UserInterestId id;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public UserInterest() {
    }

    public UserInterest(Long userId, Long interestId, LocalDateTime createdAt) {
        this.id = new UserInterestId(userId, interestId);
        this.createdAt = createdAt;
    }

    public UserInterestId getId() { return id; }
    public void setId(UserInterestId id) { this.id = id; }
    public Long getUserId() { return id == null ? null : id.getUserId(); }
    public void setUserId(Long userId) { if (id == null) id = new UserInterestId(); id.setUserId(userId); }
    public Long getInterestId() { return id == null ? null : id.getInterestId(); }
    public void setInterestId(Long interestId) { if (id == null) id = new UserInterestId(); id.setInterestId(interestId); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInterest that = (UserInterest) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UserInterest{userId=" + getUserId() + ", interestId=" + getInterestId() + "}";
    }
}
