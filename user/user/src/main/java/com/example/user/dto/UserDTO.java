package com.example.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private Integer totalOrders;
    private BigDecimal salary;
    private LocalDate joiningDate;
    private Integer attendance;
}

