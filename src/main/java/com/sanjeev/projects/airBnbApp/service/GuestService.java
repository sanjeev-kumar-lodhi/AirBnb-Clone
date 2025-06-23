package com.sanjeev.projects.airBnbApp.service;

import com.sanjeev.projects.airBnbApp.dto.GuestDto;

import java.util.List;

public interface GuestService {
    List<GuestDto> getAllGuests();

    GuestDto addNewGuest(GuestDto guestDto);

    GuestDto updateGuest(Long guestId, GuestDto guestDto);

    void deleteGuest(Long guestId);
}
