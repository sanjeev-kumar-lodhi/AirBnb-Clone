package com.sanjeev.projects.airBnbApp.repository;

import com.sanjeev.projects.airBnbApp.entity.Hotel;
import com.sanjeev.projects.airBnbApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel,Long> {
    List<Hotel> findByOwner(User user);
}
