package com.fms.controller.web;

import com.fms.service.BrandingService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.TimeUnit;

/**
 * Public, non-cached endpoint that serves the currently-active logo
 * (custom upload if any, bundled default otherwise). Used by both the
 * left sidebar of the authenticated app and the login page, so it MUST
 * be reachable without authentication.
 */
@Controller
@RequestMapping("/branding")
@RequiredArgsConstructor
public class BrandingController {

    private final BrandingService brandingService;

    @GetMapping("/logo")
    public ResponseEntity<Resource> logo() {
        Resource resource = brandingService.resolveLogoResource();
        MediaType contentType = MediaTypeFactory.getMediaType(resource.getFilename())
                .orElse(MediaType.IMAGE_PNG);
        return ResponseEntity.ok()
                // The URL itself carries a cache-busting "?v=..." token,
                // so we can let browsers cache the file for a while.
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .contentType(contentType)
                .body(resource);
    }
}
