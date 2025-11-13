package com.example.wandoor.security;


import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasAnyRole('MAKER','CHECKER','APPROVAL')")
public @interface RequireAdminRole {
}
