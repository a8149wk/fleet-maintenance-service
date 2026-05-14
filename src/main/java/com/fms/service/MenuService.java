package com.fms.service;

import com.fms.dto.menu.MenuView;
import com.fms.entity.Menu;
import com.fms.entity.Role;
import com.fms.repository.MenuRepository;
import com.fms.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Resolves the navigation entries that should appear in the sidebar
 * for the currently authenticated user, by walking
 * {@code authentication.authorities -> roles -> role_menus -> menus}.
 *
 * Designed to be cheap on each request: a single JPQL query with the
 * role names as parameters, no in-memory joins.
 */
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final RoleRepository roleRepository;

    /** Order of section blocks in the sidebar. Anything else falls to the end. */
    private static final List<String> SECTION_ORDER = List.of(
            "main", "operations", "analytics", "administration"
    );

    @Transactional(readOnly = true)
    public List<MenuView> getMenusForUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }
        Set<String> roleNames = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                .collect(Collectors.toSet());
        if (roleNames.isEmpty()) {
            return List.of();
        }
        return menuRepository.findVisibleForRoles(roleNames).stream()
                .map(this::toView)
                .toList();
    }

    /**
     * Groups menus by their {@code section} property, preserving the
     * server-side sort order. Sections themselves are ordered by
     * {@link #SECTION_ORDER} first, then alphabetically.
     */
    public LinkedHashMap<String, List<MenuView>> groupBySection(List<MenuView> menus) {
        Map<String, List<MenuView>> grouped = menus.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getSection() == null ? "" : m.getSection(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        LinkedHashMap<String, List<MenuView>> ordered = new LinkedHashMap<>();
        for (String s : SECTION_ORDER) {
            if (grouped.containsKey(s)) ordered.put(s, grouped.remove(s));
        }
        grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> ordered.put(e.getKey(), e.getValue()));
        return ordered;
    }

    @Transactional(readOnly = true)
    public List<Menu> findAll() {
        return menuRepository.findAllByOrderBySortOrderAscIdAsc();
    }

    /**
     * Returns all menus grouped by section, in canonical section order,
     * for rendering the role -> menu mapping form.
     */
    @Transactional(readOnly = true)
    public LinkedHashMap<String, List<Menu>> findAllGroupedBySection() {
        List<Menu> all = findAll();
        Map<String, List<Menu>> grouped = all.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getSection() == null ? "other" : m.getSection(),
                        LinkedHashMap::new,
                        Collectors.toList()));
        LinkedHashMap<String, List<Menu>> ordered = new LinkedHashMap<>();
        for (String s : SECTION_ORDER) {
            if (grouped.containsKey(s)) ordered.put(s, grouped.remove(s));
        }
        grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> ordered.put(e.getKey(), e.getValue()));
        return ordered;
    }

    @Transactional
    public void updateRoleMenus(Long roleId, Set<Long> menuIds) {
        Role role = roleRepository.findByIdWithMenus(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        Set<Menu> selected = menuIds == null || menuIds.isEmpty()
                ? new HashSet<>()
                : new HashSet<>(menuRepository.findAllById(menuIds));
        role.setMenus(selected);
        roleRepository.save(role);
    }

    private MenuView toView(Menu m) {
        return MenuView.builder()
                .id(m.getId())
                .code(m.getCode())
                .label(m.getLabel())
                .icon(m.getIcon())
                .path(m.getPath())
                .section(m.getSection())
                .sortOrder(m.getSortOrder())
                .build();
    }
}
