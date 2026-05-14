package com.fms.dto.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Lightweight projection of a navigation menu entry, consumed by the
 * Thymeleaf sidebar template. Kept separate from {@link com.fms.entity.Menu}
 * so the template never traverses lazy associations.
 */
@Getter
@Builder
@AllArgsConstructor
public class MenuView {
    private final Long id;
    private final String code;
    private final String label;
    private final String icon;
    private final String path;
    private final String section;
    private final Integer sortOrder;
}
