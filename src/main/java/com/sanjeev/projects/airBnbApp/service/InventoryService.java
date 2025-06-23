package com.sanjeev.projects.airBnbApp.service;

import com.sanjeev.projects.airBnbApp.dto.*;
import com.sanjeev.projects.airBnbApp.entity.Room;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {
    void initionlizRoomForAYear(Room room);
    void deleteAllInventories(Room room);


    Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);

    List<InventoryDto> getAllInventoryByRoom(Long roomId);

    void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto);
}
