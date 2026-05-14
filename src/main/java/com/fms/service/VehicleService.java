package com.fms.service;

import com.fms.entity.Vehicle;
import com.fms.exception.ResourceNotFoundException;
import com.fms.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    @Transactional(readOnly = true)
    public Page<Vehicle> search(Long clientId, String search, Pageable pageable) {
        return vehicleRepository.search(clientId, search, pageable);
    }

    @Transactional(readOnly = true)
    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
    }

    @Transactional(readOnly = true)
    public List<Vehicle> findByClient(Long clientId) {
        return vehicleRepository.findByClientId(clientId);
    }

    public Vehicle save(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public void delete(Long id) {
        vehicleRepository.deleteById(id);
    }
}
