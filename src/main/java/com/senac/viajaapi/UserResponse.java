package com.senac.viajaapi;

public class UserResponse {

    private Long id;
    private String email;
    private String name;
    private String defaultMode;

    public UserResponse() {
    }

    public UserResponse(Long id, String email, String name, String defaultMode) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.defaultMode = defaultMode;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDefaultMode() { return defaultMode; }
    public void setDefaultMode(String defaultMode) { this.defaultMode = defaultMode; }
}
