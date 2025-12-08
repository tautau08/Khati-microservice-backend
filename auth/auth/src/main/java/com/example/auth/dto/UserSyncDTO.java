package com.example.auth.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UserSyncDTO {
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private LocalDate joiningDate;
}
