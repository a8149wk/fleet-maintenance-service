package com.fms.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Links a portal user ({@code ROLE_PARTNER_WORKSHOP}) to one or more
 * {@link Workshop} rows they operate on behalf of.
 */
@Entity
@Table(name = "user_workshops",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "workshop_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWorkshop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workshop_id", nullable = false)
    private Workshop workshop;
}
