package com.senac.viajaapi;

public class LoginResponse {

    private String status;
    private String message;
    private UserResponse user;

    public LoginResponse() {
    }

    public LoginResponse(String status, String message, UserResponse user) {
        this.status = status;
        this.message = message;
        this.user = user;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }
}
