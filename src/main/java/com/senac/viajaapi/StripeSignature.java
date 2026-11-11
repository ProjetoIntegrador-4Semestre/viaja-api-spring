package com.senac.viajaapi;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

final class StripeSignature {
    private StripeSignature() {
    }

    static boolean matches(String secret, String header, byte[] body, Instant now) {
        if (header == null) return false;
        String[] fields = header.split(",");
        String timestamp = Arrays.stream(fields).map(String::trim)
                .filter(field -> field.startsWith("t=")).map(field -> field.substring(2))
                .findFirst().orElse("");
        try {
            long seconds = Long.parseLong(timestamp);
            long current = now.getEpochSecond();
            if (seconds < current - 300 || seconds > current + 300) return false;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            mac.update((timestamp + ".").getBytes(StandardCharsets.US_ASCII));
            byte[] expected = mac.doFinal(body);
            for (String field : fields) {
                String value = field.trim();
                if (value.matches("v1=[0-9a-fA-F]{64}")
                        && MessageDigest.isEqual(expected, HexFormat.of().parseHex(value.substring(3)))) {
                    return true;
                }
            }
            return false;
        } catch (IllegalArgumentException | GeneralSecurityException exception) {
            return false;
        }
    }
}
