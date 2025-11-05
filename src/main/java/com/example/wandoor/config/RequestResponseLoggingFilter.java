package com.example.wandoor.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RequestResponseLoggingFilter extends OncePerRequestFilter{

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

                ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
                ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);


                String headerCif = request.getHeader("Customer-Id");
                String headerUserId = request.getHeader("User-Id");

                // generate traceId & requestId
                String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
                String requestId = UUID.randomUUID().toString();


                // isi MDC
                MDC.put("traceId", traceId);
                MDC.put("requestId", requestId);
                MDC.put("header.Customer-Id", headerCif);
                MDC.put("header.userId", headerUserId);
                MDC.put("url", request.getRequestURI());
                MDC.put("method", request.getMethod());

                long start = System.currentTimeMillis();

                try {
                    filterChain.doFilter(requestWrapper, responseWrapper);
                } catch ( Exception e ){
                    log.error("Exception during request processing", e);
                    throw e;
                } finally {
                    long duration = System.currentTimeMillis() - start;

                    String requestBody = "";
                    if ("POST".equalsIgnoreCase(request.getMethod())
                        || "PUT".equalsIgnoreCase(request.getMethod())
                        || "PATCH".equalsIgnoreCase(request.getMethod())) {
                        byte[] buf = requestWrapper.getContentAsByteArray();
                        requestBody = new String(buf, StandardCharsets.UTF_8);
                        requestBody = requestBody.replaceAll("(?i)\"password\"\\s*:\\s*\"[^\"]+\"", "\"password\":\"***\"");
                    } else {
                        requestBody = request.getQueryString() != null ? request.getQueryString() : "";
                    };

                    String responseBody = "";
                    byte[] resBody = responseWrapper.getContentAsByteArray();
                    if (resBody.length > 2000 ){
                        responseBody = responseBody.substring(0, 2000) + "...(truncated)";
                    }

                    int status = responseWrapper.getStatus();
                    MDC.put("statusCode", String.valueOf(status));
                    MDC.put("request", requestBody);
                    MDC.put("response", responseBody);

                    log.info("HTTP {} {} -> status={} duration={}ms",
                        request.getMethod(),
                        request.getRequestURI(),
                        status,
                        duration);

                    responseWrapper.copyBodyToResponse();
                    MDC.clear();
                    // TODO: handle exception
                }
            }
    
}
