package com.fms.repository;

import com.fms.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    Optional<Menu> findByCode(String code);

    List<Menu> findAllByOrderBySortOrderAscIdAsc();

    @Query("SELECT DISTINCT m FROM Menu m "
            + "JOIN m.roles r "
            + "WHERE m.isActive = true AND r.name IN :roleNames "
            + "ORDER BY m.sortOrder ASC, m.id ASC")
    List<Menu> findVisibleForRoles(Set<String> roleNames);
}
