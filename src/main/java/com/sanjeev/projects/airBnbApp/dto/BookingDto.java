package com.sanjeev.projects.airBnbApp.dto;

import com.sanjeev.projects.airBnbApp.entity.Guest;
import com.sanjeev.projects.airBnbApp.entity.enums.BookingStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class BookingDto {

    private Long id;
    private Set<Guest> guest;
    private BookingStatus bookingStatus;
    private Integer roomsCount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal amount;
}
