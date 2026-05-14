package com.fms.service;

import com.fms.entity.Role;
import com.fms.exception.ResourceNotFoundException;
import com.fms.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<Role> findAll() {
        return roleRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Role findById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
    }

    @Transactional(readOnly = true)
    public Role findByIdWithMenus(Long id) {
        return roleRepository.findByIdWithMenus(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
    }

    public Role create(Role role) {
        roleRepository.findByName(role.getName()).ifPresent(existing -> {
            throw new IllegalArgumentException("Role name already exists: " + role.getName());
        });
        return roleRepository.save(role);
    }

    public Role update(Long id, Role form) {
        Role existing = findById(id);
        existing.setName(form.getName());
        existing.setDescription(form.getDescription());
        return roleRepository.save(existing);
    }

    public void delete(Long id) {
        roleRepository.deleteById(id);
    }
}
