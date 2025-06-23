package com.sanjeev.projects.airBnbApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDto {
    private Long id;
    private LocalDate date;
    private Integer totalCount;
    private Integer bookedCount;
    private Integer reservedCount;
    private BigDecimal price;
    private BigDecimal surgeFactor;
    private Boolean closed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
