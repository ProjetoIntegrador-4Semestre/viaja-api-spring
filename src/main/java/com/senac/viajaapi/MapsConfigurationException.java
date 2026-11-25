package com.senac.viajaapi;

public class MapsConfigurationException extends RuntimeException {

    public MapsConfigurationException() {
        super("Google Maps services are not configured");
    }
}
