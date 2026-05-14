package com.fms.config;

import com.fms.exception.BusinessException;
import com.fms.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;

/**
 * Maps domain errors from MVC controllers to flash messages and a safe redirect.
 */
@Slf4j
@ControllerAdvice(basePackages = "com.fms.controller.web")
public class WebExceptionControllerAdvice {

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex, RedirectAttributes ra, HttpServletRequest request) {
        ra.addFlashAttribute("error", ex.getMessage());
        return "redirect:" + sameHostPathOrHome(request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, RedirectAttributes ra) {
        ra.addFlashAttribute("error", ex.getMessage());
        return "redirect:/";
    }

    private static String sameHostPathOrHome(HttpServletRequest request) {
        String ref = request.getHeader("Referer");
        if (ref == null || ref.isBlank()) {
            return "/";
        }
        try {
            URI uri = URI.create(ref);
            String reqHost = request.getServerName();
            if (uri.getHost() != null && uri.getHost().equalsIgnoreCase(reqHost)) {
                String path = uri.getRawPath() == null ? "/" : uri.getRawPath();
                if (uri.getRawQuery() != null && !uri.getRawQuery().isEmpty()) {
                    path += "?" + uri.getRawQuery();
                }
                return path;
            }
        } catch (Exception e) {
            log.debug("Could not parse Referer for redirect: {}", ref, e);
        }
        return "/";
    }
}
