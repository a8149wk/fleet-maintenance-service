package com.fms.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Exposes request-scoped values (e.g. current URI for marking the
 * active navigation link) as model attributes to every Thymeleaf
 * view. Thymeleaf 3.1+ no longer exposes #httpServletRequest by
 * default, so we surface what we need explicitly here.
 */
@ControllerAdvice(basePackages = "com.fms.controller.web")
public class GlobalModelAttributes {

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
