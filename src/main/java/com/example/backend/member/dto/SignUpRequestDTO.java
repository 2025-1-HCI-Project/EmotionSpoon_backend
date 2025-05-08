package com.example.backend.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequestDTO {
    private String username;
    private String email;
    private String userId;
    private String password;
}
