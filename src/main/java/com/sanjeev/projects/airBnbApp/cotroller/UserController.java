package com.sanjeev.projects.airBnbApp.cotroller;

import com.sanjeev.projects.airBnbApp.dto.BookingDto;
import com.sanjeev.projects.airBnbApp.dto.GuestDto;
import com.sanjeev.projects.airBnbApp.dto.ProfileUpdateRequestDto;
import com.sanjeev.projects.airBnbApp.dto.UserDto;
import com.sanjeev.projects.airBnbApp.service.BookingService;
import com.sanjeev.projects.airBnbApp.service.GuestService;
import com.sanjeev.projects.airBnbApp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final BookingService bookingService;
    private final GuestService guestService;

    @PatchMapping("/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody ProfileUpdateRequestDto profileUpdateRequestDto){
        userService.updateProfile(profileUpdateRequestDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/myBookings")
    public ResponseEntity<List<BookingDto>> getMyBookings(){
        return ResponseEntity.ok(bookingService.getMyBookings());
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getMyProfile(){
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @PostMapping("/addHotelManagerRole")
    public ResponseEntity<Void> addHotelManagerRole(){
        userService.addHotelManagerRole();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/guests")
    public ResponseEntity<List<GuestDto>> getAllGuests(){
        return ResponseEntity.ok(guestService.getAllGuests());
    }

    @PostMapping("/addNewGuests")
    public ResponseEntity<GuestDto>  addNewGuest(@RequestBody GuestDto guestDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(guestService.addNewGuest(guestDto));
    }

    @PutMapping("/guests/{guestId}")
    public ResponseEntity<GuestDto> updateGuest(@PathVariable Long guestId,@RequestBody  GuestDto guestDto){
        return ResponseEntity.ok(guestService.updateGuest(guestId,guestDto));
    }

    @DeleteMapping("/guests/{guestId}")
    public ResponseEntity<Void> deleteGuest(@PathVariable Long guestId){
        guestService.deleteGuest(guestId);
        return ResponseEntity.noContent().build();
    }

}
