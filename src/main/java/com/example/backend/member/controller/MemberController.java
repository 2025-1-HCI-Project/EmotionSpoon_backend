package com.example.backend.member.controller;

import com.example.backend.member.dto.JwtResponseDTO;
import com.example.backend.member.dto.LoginRequestDTO;
import com.example.backend.member.dto.SignUpRequestDTO;
import com.example.backend.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 회원가입 엔드포인트
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequestDTO request) {
        memberService.signUp(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    // 로그인 엔드포인트
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login(@RequestBody LoginRequestDTO request) {
        JwtResponseDTO response = memberService.login(request);
        return ResponseEntity.ok(response);
    }
}
