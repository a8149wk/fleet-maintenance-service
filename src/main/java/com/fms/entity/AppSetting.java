package com.fms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Lightweight key/value store for tenant-level branding and runtime
 * configuration the admin can edit at runtime (e.g. logo path).
 * Kept intentionally generic so future "site title", "footer text",
 * "primary color" toggles can be added without new tables.
 */
@Entity
@Table(name = "app_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppSetting {

    @Id
    @Column(name = "setting_key", length = 80)
    private String key;

    /** Stored as TEXT — large enough for paths, URLs, JSON blobs, etc. */
    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String value;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
