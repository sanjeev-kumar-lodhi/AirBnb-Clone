package com.sanjeev.projects.airBnbApp.service;

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

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImp implements RoomService{

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public RoomDto createNewRoom(Long hotelId, RoomDto roomDto) {

        log.info("Creating a new  room iin  hotel with  ID : {}  ",hotelId);
        Hotel hotel = hotelRepository.findById(hotelId).
                orElseThrow(()-> new ResourceNotFoundException("Hotel not found with ID : "+hotelId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new AccessDeniedException("This user does not own this hotel with id : "+hotelId);
        }

        Room room = modelMapper.map(roomDto,Room.class);
        room.setHotel(hotel);
        room = roomRepository.save(room);
        if(hotel.getActive()){
            inventoryService.initionlizRoomForAYear(room);
        }
        return modelMapper.map(room,RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {

        log.info("Getting all room in  hotel with ID : {}  ",hotelId);
        Hotel hotel = hotelRepository.findById(hotelId).
                orElseThrow(()-> new ResourceNotFoundException("Hotel not found with ID : "+hotelId));
        return hotel.getRooms().
                stream()
                .map((element) -> modelMapper.map(element, RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long roomId) {

        log.info("Getting  the room with ID : {}  ",roomId);
        Room room = roomRepository.findById(roomId).
                orElseThrow(()-> new ResourceNotFoundException("Room not found with ID : "+roomId));
        return modelMapper.map(room,RoomDto.class);
    }

    @Override
    @Transactional
    public void deleteRoomById(Long roomId) {

        log.info("Deleting  the room with ID : {}  ",roomId);

        Room room=roomRepository.findById(roomId).
                orElseThrow(()-> new ResourceNotFoundException("Room not found with ID : "+roomId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(room.getHotel().getOwner())){
            throw new AccessDeniedException("This user does not own this room with id : "+roomId);
        }

        inventoryService.deleteAllInventories(room);

        roomRepository.deleteById(roomId);
    }

    @Override
    @Transactional
    public RoomDto updateRoomById(Long hotelId, Long roomId, RoomDto roomDto) {

         log.info("Updating the room with Id : {}",roomId);

         Hotel hotel = hotelRepository.
                 findById(hotelId).orElseThrow(()-> new ResourceNotFoundException("Hotel not found with Id : "+hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(hotel.getOwner())){
            throw new AccessDeniedException("This user does not own this room with id : "+roomId);
        }
        Room room = roomRepository.findById(roomId).
                orElseThrow(()-> new ResourceNotFoundException("Room not found with ID : "+roomId));

        modelMapper.map(roomDto,room);
        room.setId(roomId);

        room=roomRepository.save(room);

        return modelMapper.map(room,RoomDto.class);
    }
}
