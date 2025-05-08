package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ✅ CORS 설정: http://localhost:3000 허용
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true);
    }

    // ✅ Spring Security: 모든 요청 허용 + CORS 활성화
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> {}) // ✅ CORS 활성화 필수
                .csrf(csrf -> csrf.disable()) // ✅ CSRF 보호 끄기
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll() // ✅ API 경로는 모두 허용
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form.disable()) // 로그인 페이지 X
                .httpBasic(basic -> basic.disable()) // HTTP Basic X
                .build();
    }
}
