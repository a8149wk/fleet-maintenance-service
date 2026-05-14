package com.fms.config;

import com.fms.dto.menu.MenuView;
import com.fms.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Exposes request-scoped values to every Thymeleaf view rendered by
 * a {@code com.fms.controller.web} controller:
 *
 * <ul>
 *   <li>{@code currentPath} — request URI, for marking active nav links.</li>
 *   <li>{@code sidebarMenus} — section-grouped, role-filtered menu tree
 *       consumed by {@code layout/main.html}'s left sidebar.</li>
 * </ul>
 *
 * Thymeleaf 3.1+ no longer exposes {@code #httpServletRequest} by
 * default, so we surface what we need explicitly here.
 */
@ControllerAdvice(basePackages = "com.fms.controller.web")
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final MenuService menuService;

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("sidebarMenus")
    public LinkedHashMap<String, List<MenuView>> sidebarMenus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<MenuView> menus = menuService.getMenusForUser(auth);
        return menuService.groupBySection(menus);
    }
}
