package com.senac.viajaapi;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LocalSource {
    MOCK("mock"),
    GOOGLE("google"),
    CUSTOM("custom"),
    OSM("osm");

    private final String value;

    LocalSource(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static LocalSource fromValue(String value) {
        if (value == null) return null;
        return Arrays.stream(values())
                .filter(source -> source.value.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported local source: " + value));
    }
}
