package com.senac.viajaapi;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Table(name = "saved_locals", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "local_id"}))
@org.springframework.hateoas.server.core.Relation(collectionRelation = "savedLocals")
@Entity
public class SavedLocal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "local_id")
    private Long localId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public SavedLocal() {
    }

    public SavedLocal(Long userId, Long localId, LocalDateTime createdAt) {
        this.userId = userId;
        this.localId = localId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getLocalId() { return localId; }
    public void setLocalId(Long localId) { this.localId = localId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavedLocal savedLocal = (SavedLocal) o;
        return id.equals(savedLocal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SavedLocal{id=" + id + ", userId=" + userId + ", localId=" + localId + "}";
    }
}
