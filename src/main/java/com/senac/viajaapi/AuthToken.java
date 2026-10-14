package com.senac.viajaapi;

public class AuthToken {

    private String token;
    private UserResponse user;

    public AuthToken() {
    }

    public AuthToken(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }
}
