package com.example.wandoor.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;

@Component
public class OtpGuards {
    private static final int OTP_DIGITS = 6;
    private static final int OTP_BOUND = 1_000_000; // 10^6

    // reuse SecureRandom instance
    private static final SecureRandom SECURE_RANDOM = createSecureRandom();

    private OtpGuards() {}

    private static SecureRandom createSecureRandom() {
        try {
            return SecureRandom.getInstanceStrong(); // very strong if available
        } catch (NoSuchAlgorithmException e) {
            return new SecureRandom(); // fallback
        }
    }

    public static String generateNumericOtp() {
        int value = SECURE_RANDOM.nextInt(OTP_BOUND); // 0 .. 999_999
        return String.format("%0" + OTP_DIGITS + "d", value);
    }

}
