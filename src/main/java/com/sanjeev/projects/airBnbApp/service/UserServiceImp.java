package com.sanjeev.projects.airBnbApp.service;

import com.sanjeev.projects.airBnbApp.dto.ProfileUpdateRequestDto;
import com.sanjeev.projects.airBnbApp.dto.UserDto;
import com.sanjeev.projects.airBnbApp.entity.User;
import com.sanjeev.projects.airBnbApp.entity.enums.Role;
import com.sanjeev.projects.airBnbApp.exceptions.ResourceNotFoundException;
import com.sanjeev.projects.airBnbApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.sanjeev.projects.airBnbApp.util.AppUtils.getCurrentUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService , UserDetailsService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with Id :"+userId));
    }

    @Override
    public void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto) {

            User user = getCurrentUser();

            if(profileUpdateRequestDto.getDateOfBirth() != null) user.setDateOfBirth(profileUpdateRequestDto.getDateOfBirth());
            if(profileUpdateRequestDto.getName() != null) user.setName(profileUpdateRequestDto.getName());
            if (profileUpdateRequestDto.getGender() != null) user.setGender(profileUpdateRequestDto.getGender());

            userRepository.save(user);
    }

    @Override
    public UserDto getMyProfile() {

        User user = getCurrentUser();

        log.info("Getting the profile for the User with Id : {}",user.getId());
        return modelMapper.map(user,UserDto.class);
    }

    @Override
    public void addHotelManagerRole() {

        User user = getCurrentUser();

        log.info("Adding the user hotelManager role : {}",user.getId());

        user.setRoles(Set.of(Role.HOTEL_MANAGER));
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return userRepository.findByEmail(username).orElse(null);
    }
}
