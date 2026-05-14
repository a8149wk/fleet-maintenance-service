package com.fms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Navigation menu item. Renders in the left sidebar; visibility per user
 * is driven by {@link Role}'s {@code menus} mapping (the {@code role_menus}
 * link table).
 */
@Entity
@Table(name = "menus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Stable code, used in seed scripts and for URL-friendly identification. */
    @Column(nullable = false, unique = true, length = 60)
    private String code;

    @Column(nullable = false, length = 100)
    private String label;

    /** Font Awesome class, e.g. {@code fas fa-home}. Optional. */
    @Column(length = 60)
    private String icon;

    /** Application path the link navigates to. Optional for "section header" rows. */
    @Column(length = 200)
    private String path;

    /** Optional grouping section label rendered above the item in the sidebar. */
    @Column(length = 50)
    private String section;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "menus", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
