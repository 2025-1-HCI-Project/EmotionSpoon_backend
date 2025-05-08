package com.example.backend.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResponseDTO {
    private String token;
    private String username;
    private String email;
    private String userId;
    private Long memberId;
}
