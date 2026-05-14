package com.fms.service;

import com.fms.entity.Workshop;
import com.fms.enums.WorkshopType;
import com.fms.exception.ResourceNotFoundException;
import com.fms.repository.WorkshopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkshopService {

    private final WorkshopRepository workshopRepository;

    @Transactional(readOnly = true)
    public Page<Workshop> search(WorkshopType type, String search, Pageable pageable) {
        return workshopRepository.search(type, search == null ? "" : search, pageable);
    }

    @Transactional(readOnly = true)
    public List<Workshop> findActive() {
        return workshopRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public Workshop findById(Long id) {
        return workshopRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Workshop", id));
    }

    public Workshop save(Workshop workshop) {
        if (workshop.getCode() == null || workshop.getCode().isBlank()) {
            workshop.setCode(String.format("WS%03d", workshopRepository.count() + 1));
        }
        return workshopRepository.save(workshop);
    }

    public void delete(Long id) {
        workshopRepository.deleteById(id);
    }
}
