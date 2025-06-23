package com.sanjeev.projects.airBnbApp.security;

import com.sanjeev.projects.airBnbApp.dto.LoginDto;
import com.sanjeev.projects.airBnbApp.dto.SignUpRequestDto;
import com.sanjeev.projects.airBnbApp.dto.UserDto;
import com.sanjeev.projects.airBnbApp.entity.User;
import com.sanjeev.projects.airBnbApp.entity.enums.Role;
import com.sanjeev.projects.airBnbApp.exceptions.ResourceNotFoundException;
import com.sanjeev.projects.airBnbApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final  JWTService jwtService;
    private final PasswordEncoder passwordEncoder;


    public UserDto signUp(SignUpRequestDto signUpRequestDto){

        User user = userRepository.findByEmail(signUpRequestDto.getEmail()).orElse(null);

        if(user!=null){
            throw new RuntimeException("User already present with same Email Id  ");
        }

        User newUser = modelMapper.map(signUpRequestDto,User.class);
        newUser.setRoles(Set.of(Role.GUEST));
        newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
        newUser=userRepository.save(newUser);

        return modelMapper.map(newUser,UserDto.class);
    }

    public String[] login(LoginDto loginDto){

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getEmail(),loginDto.getPassword()
        ));
        User user = (User) authentication.getPrincipal();

        String arr[] = new String[2];
        arr[0] = jwtService.generateAccessToken(user);
        arr[1] = jwtService.generateRefreshToken(user);

        return arr;
    }

    public String refreshToken(String refreshToken) {

        Long id = jwtService.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("User not found with id :"+id));
        return jwtService.generateAccessToken(user);
    }
}
