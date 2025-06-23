package com.sanjeev.projects.airBnbApp.repository;

import com.sanjeev.projects.airBnbApp.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room,Long> {
}
