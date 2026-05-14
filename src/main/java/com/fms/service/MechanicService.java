package com.fms.service;

import com.fms.entity.Mechanic;
import com.fms.exception.ResourceNotFoundException;
import com.fms.repository.MechanicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MechanicService {

    private final MechanicRepository mechanicRepository;

    @Transactional(readOnly = true)
    public Page<Mechanic> search(Long workshopId, String search, Pageable pageable) {
        return mechanicRepository.search(workshopId, search == null ? "" : search, pageable);
    }

    @Transactional(readOnly = true)
    public List<Mechanic> findByWorkshop(Long workshopId) {
        return mechanicRepository.findByWorkshopIdAndIsActiveTrue(workshopId);
    }

    @Transactional(readOnly = true)
    public List<Mechanic> findActive() {
        return mechanicRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public Mechanic findById(Long id) {
        return mechanicRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Mechanic", id));
    }

    public Mechanic save(Mechanic mechanic) {
        if (mechanic.getCode() == null || mechanic.getCode().isBlank()) {
            mechanic.setCode(String.format("MEC%03d", mechanicRepository.count() + 1));
        }
        return mechanicRepository.save(mechanic);
    }

    public void delete(Long id) {
        mechanicRepository.deleteById(id);
    }
}
