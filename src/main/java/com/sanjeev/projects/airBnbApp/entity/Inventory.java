package com.sanjeev.projects.airBnbApp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "unique_hotel_room_date"
                ,columnNames = {"hotel_id","room_id","date"}
        )
)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id",nullable = false)
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id",nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer totalCount;

    @Column(columnDefinition = "INTEGER DEFAULT 0",nullable = false)
    private Integer bookedCount;

    @Column(columnDefinition = "INTEGER DEFAULT 0",nullable = false)
    private Integer reservedCount;

    @Column(nullable = false,precision = 10,scale = 2)
    private BigDecimal price;

    @Column(nullable = false,precision = 5,scale = 2)
    private BigDecimal surgeFactor;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private Boolean closed;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
