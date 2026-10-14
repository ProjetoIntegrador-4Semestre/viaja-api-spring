package com.senac.viajaapi;

import java.util.List;

public class PlaceResponse {

    private String id;
    private String source;
    private String sourceId;
    private String name;
    private String address;
    private Double lat;
    private Double lng;
    private List<String> categories;
    private Float rating;
    private Integer ratingCount;
    private Integer priceLevel;
    private String priceHint;
    private String photoUrl;
    private Boolean openNow;
    private List<String> tags;

    public PlaceResponse() {
    }

    public PlaceResponse(String id, String source, String sourceId, String name, String address,
            Double lat, Double lng, List<String> categories, Float rating, Integer ratingCount,
            Integer priceLevel, String priceHint, String photoUrl, Boolean openNow, List<String> tags) {
        this.id = id;
        this.source = source;
        this.sourceId = sourceId;
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.categories = categories;
        this.rating = rating;
        this.ratingCount = ratingCount;
        this.priceLevel = priceLevel;
        this.priceHint = priceHint;
        this.photoUrl = photoUrl;
        this.openNow = openNow;
        this.tags = tags;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }
    public Float getRating() { return rating; }
    public void setRating(Float rating) { this.rating = rating; }
    public Integer getRatingCount() { return ratingCount; }
    public void setRatingCount(Integer ratingCount) { this.ratingCount = ratingCount; }
    public Integer getPriceLevel() { return priceLevel; }
    public void setPriceLevel(Integer priceLevel) { this.priceLevel = priceLevel; }
    public String getPriceHint() { return priceHint; }
    public void setPriceHint(String priceHint) { this.priceHint = priceHint; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public Boolean getOpenNow() { return openNow; }
    public void setOpenNow(Boolean openNow) { this.openNow = openNow; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
