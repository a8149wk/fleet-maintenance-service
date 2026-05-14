package com.fms.service;

import com.fms.entity.Client;
import com.fms.exception.ResourceNotFoundException;
import com.fms.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public Page<Client> search(String search, Pageable pageable) {
        return clientRepository.search(search == null ? "" : search, pageable);
    }

    @Transactional(readOnly = true)
    public List<Client> findActive() {
        return clientRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public Client findById(Long id) {
        return clientRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }

    public Client save(Client client) {
        if (client.getCode() == null || client.getCode().isBlank()) {
            client.setCode(generateCode());
        }
        return clientRepository.save(client);
    }

    public void delete(Long id) {
        clientRepository.deleteById(id);
    }

    private String generateCode() {
        long count = clientRepository.count() + 1;
        return String.format("CLI%03d", count);
    }
}
