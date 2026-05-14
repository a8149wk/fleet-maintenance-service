package com.fms.repository;

import com.fms.entity.UserWorkshop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserWorkshopRepository extends JpaRepository<UserWorkshop, Long> {

    List<UserWorkshop> findByUserId(Long userId);

    List<UserWorkshop> findByWorkshopId(Long workshopId);

    boolean existsByUserIdAndWorkshopId(Long userId, Long workshopId);
}
