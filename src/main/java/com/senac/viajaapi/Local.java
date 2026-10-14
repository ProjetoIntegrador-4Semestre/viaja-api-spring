package com.senac.viajaapi;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name = "locals")
@Entity
public class Local {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city_id")
    private Long cityId;

    private String type;

    private String name;

    private String address;

    private BigDecimal lat;

    private BigDecimal lng;

    private Integer price;

    private Float rating;

    @Column(name = "rating_count")
    private Integer ratingCount;

    @Column(name = "opening_hours")
    private String openingHours;

    private String description;

    private String phone;

    private String website;

    private String source;

    @Column(name = "source_id")
    private String sourceId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Local() {
    }

    public Local(Long cityId, String type, String name, String address, BigDecimal lat, BigDecimal lng,
            Integer price, Float rating, Integer ratingCount, String openingHours, String description,
            String phone, String website, String source, String sourceId, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.cityId = cityId;
        this.type = type;
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.price = price;
        this.rating = rating;
        this.ratingCount = ratingCount;
        this.openingHours = openingHours;
        this.description = description;
        this.phone = phone;
        this.website = website;
        this.source = source;
        this.sourceId = sourceId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCityId() { return cityId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public BigDecimal getLat() { return lat; }
    public void setLat(BigDecimal lat) { this.lat = lat; }
    public BigDecimal getLng() { return lng; }
    public void setLng(BigDecimal lng) { this.lng = lng; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public Float getRating() { return rating; }
    public void setRating(Float rating) { this.rating = rating; }
    public Integer getRatingCount() { return ratingCount; }
    public void setRatingCount(Integer ratingCount) { this.ratingCount = ratingCount; }
    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Local local = (Local) o;
        return id.equals(local.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Local{id=" + id + ", name='" + name + "'}";
    }
}
