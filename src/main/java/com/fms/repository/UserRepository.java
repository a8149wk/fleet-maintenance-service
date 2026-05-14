package com.fms.repository;

import com.fms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE "
            + "(:search = '' OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "                OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :search, '%')) "
            + "                OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))) "
            + "ORDER BY u.id ASC")
    Page<User> search(@Param("search") String search, Pageable pageable);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE u.isActive = true AND r.name IN :roleNames")
    List<User> findAllActiveHavingAnyRole(@Param("roleNames") Collection<String> roleNames);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE u.isActive = true AND r.name = :roleName ORDER BY u.fullName")
    List<User> findActiveByRoleName(@Param("roleName") String roleName);
}
