package com.fms.repository;

import com.fms.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByCode(String code);

    boolean existsByCode(String code);

    Page<Client> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Client> findByIsActiveTrue();

    @Query("SELECT c FROM Client c WHERE " +
            "(:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Client> search(@Param("search") String search, Pageable pageable);
}
