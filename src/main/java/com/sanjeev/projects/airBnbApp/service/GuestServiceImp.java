package com.sanjeev.projects.airBnbApp.service;

import com.sanjeev.projects.airBnbApp.dto.GuestDto;
import com.sanjeev.projects.airBnbApp.entity.Guest;
import com.sanjeev.projects.airBnbApp.entity.User;
import com.sanjeev.projects.airBnbApp.exceptions.ResourceNotFoundException;
import com.sanjeev.projects.airBnbApp.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.sanjeev.projects.airBnbApp.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuestServiceImp implements GuestService{

    private final ModelMapper modelMapper;
    private final GuestRepository guestRepository;

    @Override
    public List<GuestDto> getAllGuests() {
        User user = getCurrentUser();
        log.info("Fetching all guests of user with Id : {}",user.getId());

        List<Guest> guests = guestRepository.findByUser(user);

        return guests
                .stream()
                .map((element) -> modelMapper.map(element, GuestDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public GuestDto addNewGuest(GuestDto guestDto) {
        User user = getCurrentUser();
        log.info("Adding new guest : {}",guestDto);

        Guest guest = modelMapper.map(guestDto,Guest.class);
        guest.setUser(user);
        guest = guestRepository.save(guest);
        log.info("Guest added with Id : {}",guest.getId());
        return modelMapper.map(guest,GuestDto.class);
    }

    @Override
    public GuestDto updateGuest(Long guestId, GuestDto guestDto) {
        User user = getCurrentUser();

        log.info("Updating guest with Id : {}",guestId);

        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(()-> new ResourceNotFoundException("Guest not found with Id : "+guestId));

        if(!user.equals(guest.getUser())){
            throw new AccessDeniedException("You are not the owner of this guest");
        }

        modelMapper.map(guestDto,guest);
        guest.setUser(user);
        guest.setId(guestId);

        guestRepository.save(guest);

        log.info("Guest updated with Id : {}",guestId);
        return modelMapper.map(guest,GuestDto.class);
    }

    @Override
    public void deleteGuest(Long guestId) {
        log.info("Deleting the guest with Id : {}",guestId);

        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(()-> new ResourceNotFoundException("Guest not found with Id : "+guestId));

        User user = getCurrentUser();
        if(!user.equals(guest.getUser())){
            throw new AccessDeniedException("You are not the owner of this guest");
        }

        guestRepository.deleteById(guestId);
        log.info("Guest with Id : {} deleted successfully ",guestId);
    }
}
