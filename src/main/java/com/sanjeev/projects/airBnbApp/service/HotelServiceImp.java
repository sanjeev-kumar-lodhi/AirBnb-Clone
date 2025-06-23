package com.sanjeev.projects.airBnbApp.service;


import com.sanjeev.projects.airBnbApp.dto.HotelDto;
import com.sanjeev.projects.airBnbApp.dto.HotelInfoDto;
import com.sanjeev.projects.airBnbApp.dto.RoomDto;
import com.sanjeev.projects.airBnbApp.entity.Hotel;
import com.sanjeev.projects.airBnbApp.entity.Room;
import com.sanjeev.projects.airBnbApp.entity.User;
import com.sanjeev.projects.airBnbApp.exceptions.ResourceNotFoundException;
import com.sanjeev.projects.airBnbApp.repository.HotelRepository;
import com.sanjeev.projects.airBnbApp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.sanjeev.projects.airBnbApp.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImp implements HotelService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {

        log.info("Creating a new hotel with name : {}",hotelDto.getName());

        Hotel hotel = modelMapper.map(hotelDto,Hotel.class);
        hotel.setActive(false);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        hotel.setOwner(user);
        hotel = hotelRepository.save(hotel);
        log.info("Created the hotel with name : {}",hotelDto.getName());
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long hotelId) {

        log.info("Getting  the hotel with ID : {}  ", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId).
                orElseThrow(()-> new RuntimeException("Hotel not found with ID : "+ hotelId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new AccessDeniedException("This user does not own this hotel with id : "+hotelId);
        }
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long hotelId, HotelDto hotelDto) {

        log.info("Updating  the hotel with ID : {}  ", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId).
                orElseThrow(()-> new ResourceNotFoundException("Hotel not found with ID : "+ hotelId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new AccessDeniedException("This user does not own this hotel with id : "+hotelId);
        }
        modelMapper.map(hotelDto,hotel);
        hotel.setId(hotelId);
        hotel = hotelRepository.save(hotel);
        return modelMapper.map(hotel,HotelDto.class) ;
    }

    @Override
    @Transactional
    public void deleteHotelById(Long hotelId) {

        log.info("Deleting  the hotel with ID : {}  ", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId).
                orElseThrow(()-> new ResourceNotFoundException("Hotel not found with ID : "+ hotelId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new AccessDeniedException("This user does not own this hotel with id : "+hotelId);
        }

        for(Room room:hotel.getRooms()){
            inventoryService.deleteAllInventories(room);
            roomRepository.deleteById(room.getId());
        }
       hotelRepository.deleteById(hotelId);



    }

    @Override
    @Transactional
    public void activateHotel(Long hotelId) {

        log.info("Activating the hotel with ID : {}  ", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId).
                orElseThrow(()-> new ResourceNotFoundException("Hotel not found with ID : "+ hotelId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(hotel.getOwner())){
            throw new AccessDeniedException("This user does not own this hotel with id : "+hotelId);
        }

        hotel.setActive(true);
        hotelRepository.save(hotel);
        // assuming
        for(Room room:hotel.getRooms()){
            inventoryService.initionlizRoomForAYear(room);
        }
    }

    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId) {

        Hotel hotel = hotelRepository.findById(hotelId).
                orElseThrow(()-> new ResourceNotFoundException("Hotel not found with ID : "+ hotelId));
        List<RoomDto> rooms = hotel.getRooms().
                stream().map((element) -> modelMapper.map(element, RoomDto.class)).
                collect(Collectors.toList());
        return new HotelInfoDto(modelMapper.map(hotel,HotelDto.class),rooms);
    }

    @Override
    public List<HotelDto> getAllHotels() {

        User user = getCurrentUser();
        List<Hotel> hotels = hotelRepository.findByOwner(user);
        log.info("Getting all the hotels for the admin user with Id : {}",user.getId());

        return  hotels
                .stream()
                .map(hotelEntity->modelMapper.map(hotelEntity,HotelDto.class))
                .collect(Collectors.toList());
    }


}
