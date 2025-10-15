package com.example.wandoor.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class OtpGuards {
//    private final StringRedisTemplate redis;
//
//    private String issueKey(String userId) {
//        return "otp:issue" + userId;
//    }
//
//    private String attemptskey(String userId){
//        return "otp:attempts:" + userId;
//    }
//
//    public long onIssue(String userId, Duration window){
//        var key = issueKey(userId);
//        var c = redis.opsForValue().increment(key);
//        if (c != null && c == 1L){
//            redis.expire(key, window);
//        }
//        return c == null ? 0L : c;
//    }
//
//    public long onFail(String userId, Duration window){
//        var key = attemptskey(userId);
//        var c = redis.opsForValue().increment(key);
//        if (c != null && c == 1L){
//            redis.expire(key, window);
//        }
//        return c == null ? 0L : c;
//    }
//
//    public void resetAll(String userId){
//        redis.delete(List.of(issueKey(userId), attemptskey(userId)));
//    }

}
