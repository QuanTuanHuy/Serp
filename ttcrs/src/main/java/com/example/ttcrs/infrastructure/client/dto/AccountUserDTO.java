package com.example.ttcrs.infrastructure.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountUserDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String status;

    public String getFullName() {
        String f = firstName != null ? firstName.trim() : "";
        String l = lastName != null ? lastName.trim() : "";
        String full = (f + " " + l).trim();
        return full.isEmpty() ? email : full;
    }
}
