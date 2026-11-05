package com.senac.viajaapi;

public class TripRequest {

    private Long cityId;
    private Long userId;
    private String name;
    private String mode;
    private String startDate;
    private String endDate;
    private Long budgetCents;
    private String status;

    public TripRequest() {
    }

    public TripRequest(Long userId, String name, String mode, String startDate, String endDate, Long budgetCents,
            String status) {
        this.userId = userId;
        this.name = name;
        this.mode = mode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budgetCents = budgetCents;
        this.status = status;
    }

    public Long getCityId() { return cityId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public Long getBudgetCents() { return budgetCents; }
    public void setBudgetCents(Long budgetCents) { this.budgetCents = budgetCents; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
