package com.sanjeev.projects.airBnbApp.repository;

import com.sanjeev.projects.airBnbApp.entity.Booking;
import com.sanjeev.projects.airBnbApp.entity.Hotel;
import com.sanjeev.projects.airBnbApp.entity.User;
import org.apache.logging.log4j.simple.internal.SimpleProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking,Long> {
    

    Optional<Booking> findByPaymentSessionId(String sessionId);

    List<Booking> findByHotel(Hotel hotel);

    List<Booking> findByUser(User user);

    List<Booking> findByHotelAndCreatedAtBetween(Hotel hotel, LocalDateTime startDateTime, LocalDateTime endDateTime);
}
