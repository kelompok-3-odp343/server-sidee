package com.example.wandoor.util;

import org.springframework.stereotype.Component;

@Component
public class Helpers {
    public boolean safeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int r = 0;
        for (int i = 0; i < a.length(); i++) r |= a.charAt(i) ^ b.charAt(i);
        return r == 0;
    }

    public String issuejwt(String userId, String role){
        return "test-jwt";
    }
}
