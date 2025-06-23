package com.sanjeev.projects.airBnbApp.dto;

import com.sanjeev.projects.airBnbApp.entity.User;
import com.sanjeev.projects.airBnbApp.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GuestDto {
    private Long id;
    private String name;
    private Gender gender;
    private Integer age;
}
