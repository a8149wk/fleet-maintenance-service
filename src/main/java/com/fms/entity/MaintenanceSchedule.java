package com.fms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "schedule_type", nullable = false, length = 30)
    private String scheduleType;

    @Column(name = "service_name", nullable = false, length = 200)
    private String serviceName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "interval_type", length = 20)
    private String intervalType;

    @Column(name = "interval_value")
    private Integer intervalValue;

    @Column(name = "last_service_date")
    private LocalDate lastServiceDate;

    @Column(name = "last_service_odometer")
    private Integer lastServiceOdometer;

    @Column(name = "next_service_date")
    private LocalDate nextServiceDate;

    @Column(name = "next_service_odometer")
    private Integer nextServiceOdometer;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "reminder_sent")
    @Builder.Default
    private Boolean reminderSent = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
