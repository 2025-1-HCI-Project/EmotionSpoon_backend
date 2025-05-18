package com.example.backend.diary;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

public class RecommendClient {
    public static void main(String[] args) {
        // RestTemplate 객체 생성
        RestTemplate restTemplate = new RestTemplate();

        // request header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // request body
        Map<String, Object> body = new HashMap<>();
        body.put("input_data", "여기에 일기 넣기");

        // HttpEntity 생성 (헤더 + 바디)
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // http request
        String url = "http://172.17.195.105:5050/recommend";
        String response = restTemplate.postForObject(url, request, String.class);

        System.out.println("Response: " + response);
    }
}