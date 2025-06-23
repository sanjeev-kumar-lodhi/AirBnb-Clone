package com.sanjeev.projects.airBnbApp.service;

import com.sanjeev.projects.airBnbApp.dto.ProfileUpdateRequestDto;
import com.sanjeev.projects.airBnbApp.dto.UserDto;
import com.sanjeev.projects.airBnbApp.entity.User;

public interface UserService {

     public User getUserById(Long userId);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();

    void addHotelManagerRole();
}
