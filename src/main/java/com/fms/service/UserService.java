package com.fms.service;

import com.fms.entity.Role;
import com.fms.entity.User;
import com.fms.exception.ResourceNotFoundException;
import com.fms.repository.RoleRepository;
import com.fms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<User> search(String keyword, Pageable pageable) {
        String q = keyword == null ? "" : keyword.trim();
        return userRepository.search(q, pageable);
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User " + username + " not found"));
    }

    public User create(User user, String rawPassword) {
        user.setPassword(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    /**
     * Admin create: validates uniqueness, encodes password, assigns roles.
     */
    public User adminCreate(User user, String rawPassword, Set<Long> roleIds) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        if (!StringUtils.hasText(rawPassword)) {
            throw new IllegalArgumentException("Password is required");
        }
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setIsActive(Boolean.TRUE.equals(user.getIsActive()) || user.getIsActive() == null);
        user.setIsLocked(Boolean.TRUE.equals(user.getIsLocked()));
        user.setRoles(resolveRoles(roleIds));
        return userRepository.save(user);
    }

    /**
     * Admin update: preserves password unless {@code rawPassword} is provided,
     * never overwrites the persisted password hash with the form-bound value.
     */
    public User adminUpdate(Long id, User formUser, String rawPassword, Set<Long> roleIds) {
        User existing = findById(id);
        existing.setEmail(formUser.getEmail());
        existing.setFullName(formUser.getFullName());
        existing.setPhone(formUser.getPhone());
        // Unchecked checkboxes are not sent in the form post, so a null
        // here means "off". Boolean.TRUE.equals(null) → false, which is
        // the behaviour we want for both flags.
        existing.setIsActive(Boolean.TRUE.equals(formUser.getIsActive()));
        existing.setIsLocked(Boolean.TRUE.equals(formUser.getIsLocked()));
        existing.setRoles(resolveRoles(roleIds));
        if (StringUtils.hasText(rawPassword)) {
            existing.setPassword(passwordEncoder.encode(rawPassword));
            existing.setFailedLoginAttempts(0);
        }
        return userRepository.save(existing);
    }

    public void toggleActive(Long id) {
        User u = findById(id);
        u.setIsActive(!Boolean.TRUE.equals(u.getIsActive()));
        userRepository.save(u);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    private Set<Role> resolveRoles(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(roleRepository.findAllById(roleIds));
    }
}
