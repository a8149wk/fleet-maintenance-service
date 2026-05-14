package com.fms.controller.api;

import com.fms.entity.Notification;
import com.fms.entity.User;
import com.fms.security.SecurityUtils;
import com.fms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mobile")
@RequiredArgsConstructor
public class MobileProfileController {

    private final SecurityUtils securityUtils;
    private final NotificationService notificationService;

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> profile() {
        User user = securityUtils.getCurrentUser();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("fullName", user.getFullName());
        map.put("phone", user.getPhone());
        map.put("roles", user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()));
        return ResponseEntity.ok(map);
    }

    @GetMapping("/notifications")
    public ResponseEntity<Page<Notification>> notifications() {
        User user = securityUtils.getCurrentUser();
        Page<Notification> page = notificationService.findForUser(user.getId(), PageRequest.of(0, 50));
        return ResponseEntity.ok(page);
    }

    @PostMapping("/notifications/{id}/read")
    public ResponseEntity<Void> read(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }
}
