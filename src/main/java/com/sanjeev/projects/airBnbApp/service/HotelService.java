package com.sanjeev.projects.airBnbApp.service;

import com.sanjeev.projects.airBnbApp.dto.HotelDto;
import com.sanjeev.projects.airBnbApp.dto.HotelInfoDto;

import java.util.List;

public interface HotelService {
    HotelDto createNewHotel(HotelDto hotelDto);

    HotelDto getHotelById(Long hotelId);

    HotelDto updateHotelById(Long hotelId, HotelDto hotelDto);

    void deleteHotelById(Long hotelId);

    void activateHotel(Long hotelId);


    HotelInfoDto getHotelInfoById(Long hotelId);

    List<HotelDto> getAllHotels();


}
