package com.pvr.primenaturals.dto.response;

import com.pvr.primenaturals.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String address;
    private String phoneNumber;
    private String avatarUrl;
    private Role role;
    private LocalDateTime createdAt;
}
