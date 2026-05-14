package com.fms.service;

import com.fms.entity.SparePart;
import com.fms.exception.ResourceNotFoundException;
import com.fms.repository.SparePartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SparePartService {

    private final SparePartRepository sparePartRepository;

    @Transactional(readOnly = true)
    public Page<SparePart> search(String category, String search, Pageable pageable) {
        return sparePartRepository.search(
                category == null ? "" : category,
                search == null ? "" : search,
                pageable);
    }

    @Transactional(readOnly = true)
    public List<SparePart> findAll() {
        return sparePartRepository.findAll();
    }

    @Transactional(readOnly = true)
    public SparePart findById(Long id) {
        return sparePartRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("SparePart", id));
    }

    public SparePart save(SparePart sparePart) {
        return sparePartRepository.save(sparePart);
    }

    public void delete(Long id) {
        sparePartRepository.deleteById(id);
    }
}
