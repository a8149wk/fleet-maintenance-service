package com.fms.service;

import com.fms.entity.AppSetting;
import com.fms.repository.AppSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Set;

/**
 * Manages the application logo (and surrounding "branding" settings).
 *
 * Storage layout:
 * <pre>
 *   {file.upload-dir}/branding/logo-{epoch}.{ext}
 * </pre>
 * Only the path to the active file is kept in {@code app_settings.LOGO_PATH};
 * older files are deleted on successful upload to keep the directory tidy.
 *
 * When the admin has not uploaded a custom logo yet, the service falls
 * back to the bundled {@code classpath:/static/img/logo.png}.
 */
@Service
@RequiredArgsConstructor
public class BrandingService {

    public static final String LOGO_PATH_KEY = "LOGO_PATH";
    public static final String DEFAULT_LOGO_CLASSPATH = "static/img/logo.png";

    /** Subset of image content-types we'll accept for upload. */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/svg+xml",
            "image/webp", "image/gif", "image/x-icon", "image/vnd.microsoft.icon"
    );

    /** ~2 MB hard ceiling for a logo upload. */
    private static final long MAX_BYTES = 2L * 1024L * 1024L;

    private final AppSettingRepository settingRepository;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * Resolve the binary resource that should be served for the current
     * logo. Falls back to the bundled default when no custom logo exists.
     */
    @Transactional(readOnly = true)
    public Resource resolveLogoResource() {
        Optional<Path> custom = currentLogoPath();
        if (custom.isPresent() && Files.exists(custom.get())) {
            return new FileSystemResource(custom.get());
        }
        return new ClassPathResource(DEFAULT_LOGO_CLASSPATH);
    }

    /**
     * Cache-busting URL the templates render. Hashed off the configured
     * filename (or "default" when no custom upload exists) so we don't
     * hit the DB just to mint a URL — but updates are still visible
     * immediately because uploads always rename the file.
     */
    @Transactional(readOnly = true)
    public String getLogoUrl() {
        String fingerprint = settingRepository.findById(LOGO_PATH_KEY)
                .map(AppSetting::getValue)
                .filter(StringUtils::hasText)
                .map(p -> Integer.toUnsignedString(p.hashCode()))
                .orElse("default");
        return "/branding/logo?v=" + fingerprint;
    }

    @Transactional(readOnly = true)
    public boolean hasCustomLogo() {
        return currentLogoPath().map(Files::exists).orElse(false);
    }

    /**
     * Persist the uploaded file to the branding directory and update the
     * {@code LOGO_PATH} setting. Returns the absolute path that was saved.
     */
    @Transactional
    public Path updateLogo(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select a logo file to upload.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException(
                    "Logo file is too large (" + file.getSize() + " bytes). Max 2 MB.");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported image type: " + contentType);
        }

        Path brandingDir = Paths.get(uploadDir, "branding").toAbsolutePath().normalize();
        Files.createDirectories(brandingDir);

        String ext = pickExtension(file.getOriginalFilename(), contentType);
        Path target = brandingDir.resolve("logo-" + System.currentTimeMillis() + ext);

        try (var in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        // Remove the previously-active file so the upload dir doesn't grow forever.
        currentLogoPath().ifPresent(prev -> {
            if (!prev.equals(target)) {
                try {
                    Files.deleteIfExists(prev);
                } catch (IOException ignored) {
                    // best-effort cleanup
                }
            }
        });

        AppSetting setting = settingRepository.findById(LOGO_PATH_KEY)
                .orElseGet(() -> AppSetting.builder().key(LOGO_PATH_KEY).build());
        setting.setValue(target.toString());
        settingRepository.save(setting);

        return target;
    }

    @Transactional
    public void resetLogo() {
        currentLogoPath().ifPresent(p -> {
            try {
                Files.deleteIfExists(p);
            } catch (IOException ignored) {
                // best-effort
            }
        });
        AppSetting setting = settingRepository.findById(LOGO_PATH_KEY)
                .orElseGet(() -> AppSetting.builder().key(LOGO_PATH_KEY).build());
        setting.setValue(null);
        settingRepository.save(setting);
    }

    private Optional<Path> currentLogoPath() {
        return settingRepository.findById(LOGO_PATH_KEY)
                .map(AppSetting::getValue)
                .filter(StringUtils::hasText)
                .map(Paths::get);
    }

    private String pickExtension(String originalName, String contentType) {
        if (originalName != null) {
            int dot = originalName.lastIndexOf('.');
            if (dot >= 0 && dot < originalName.length() - 1) {
                String fromName = originalName.substring(dot).toLowerCase();
                if (fromName.length() <= 5) return fromName;
            }
        }
        return switch (contentType) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/svg+xml" -> ".svg";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            case "image/x-icon", "image/vnd.microsoft.icon" -> ".ico";
            default -> ".png";
        };
    }
}
