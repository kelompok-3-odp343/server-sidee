package com.example.wandoor.config;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class RequestContext {
    private static final ThreadLocal<RequestContext> CONTEXT = ThreadLocal.withInitial(RequestContext::new);

    private String userId;
    private String cif;
    private String npp;
    private String token;

    public static RequestContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
