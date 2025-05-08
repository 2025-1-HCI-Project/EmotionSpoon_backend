package com.example.backend.member.service;

import com.example.backend.config.JwtToken;
import com.example.backend.member.dto.*;
import com.example.backend.member.entity.Member;
import com.example.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtToken jwtToken;

    public void signUp(SignUpRequestDTO request) {
        if (memberRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        if (memberRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        Member member = new Member();
        member.setUsername(request.getUsername());
        member.setEmail(request.getEmail());
        member.setUserId(request.getUserId());
        member.setPassword(passwordEncoder.encode(request.getPassword()));

        memberRepository.save(member);
    }

    public JwtResponseDTO login(LoginRequestDTO request) {
        Member member = memberRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("아이디를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtToken.createToken(member.getUserId());

        JwtResponseDTO response = new JwtResponseDTO();
        response.setToken(token);
        response.setUsername(member.getUsername());
        response.setEmail(member.getEmail());
        response.setMemberId(member.getId());
        return response;
    }
}
