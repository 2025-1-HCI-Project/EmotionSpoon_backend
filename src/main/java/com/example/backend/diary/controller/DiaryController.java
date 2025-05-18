package com.example.backend.diary.controller;

import com.example.backend.diary.dto.DiaryDTO;
import com.example.backend.diary.entity.Diary;
import com.example.backend.diary.repository.DiaryRepository;
import com.example.backend.member.entity.Member;
import com.example.backend.member.repository.MemberRepository;
import com.example.backend.playlist.entity.Playlist;
import com.example.backend.playlist.repository.PlaylistRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;
    private final PlaylistRepository playlistRepository;

    @Value("${file.diary-dir}")
    private String diaryDir;

    @PostMapping("/upload")
    @Transactional
    public ResponseEntity<?> uploadDiary(
            @RequestPart("dto") DiaryDTO diaryDTO,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {

        Member member = memberRepository.findById(diaryDTO.getMemberId())
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        String storedFileName = null;
        String fullText = diaryDTO.getDiaryContent();

        if (file != null && !file.isEmpty()) {
            // 경로 준비
            Path storagePath = Paths.get(diaryDir).toAbsolutePath().normalize();
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }

            // 파일 이름 생성 및 경로 설정
            String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            storedFileName = UUID.randomUUID() + fileExtension;
            Path filePath = storagePath.resolve(storedFileName);

            // 1. OCR을 위한 바이트 읽기 (먼저)
            byte[] fileBytes;
            try (InputStream is = file.getInputStream()) {
                fileBytes = is.readAllBytes();
            }

            // 2. FastAPI OCR 서버에 이미지 업로드
            String fastApiImageUrl = "http://0.0.0.0:5050/upload";
            HttpHeaders imgHeaders = new HttpHeaders();
            imgHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

            ByteArrayResource resource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return originalFileName;
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);
            HttpEntity<MultiValueMap<String, Object>> imageRequest = new HttpEntity<>(body, imgHeaders);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> imgResponse = restTemplate.postForEntity(fastApiImageUrl, imageRequest, Map.class);
            fullText = (String) imgResponse.getBody().get("text");

            // 3. 파일 저장은 마지막에
            file.transferTo(filePath.toFile());
        }

        // 감정 분석 및 추천곡 요청
        String fastApiRunUrl = "http://0.0.0.0:5050/run";
        Map<String, String> payload = new HashMap<>();
        payload.put("input_data", fullText);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(payload, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(fastApiRunUrl, requestEntity, Map.class);

        String sentiment = (String) response.getBody().get("sentiment");
        String artist = (String) response.getBody().get("artist");
        String song = (String) response.getBody().get("song");
        String url = (String) response.getBody().get("link");

        // Diary 저장
        LocalDateTime diaryDate = LocalDateTime.parse(diaryDTO.getDate() + "T00:00:00");
        Diary diary = new Diary();
        diary.setMember(member);
        diary.setDate(diaryDate);
        diary.setContent(diaryDTO.getTitle());
        diary.setDiaryContent(diaryDTO.getDiaryContent());
        diary.setFileName(storedFileName);
        diary.setDiaryType((file != null && !file.isEmpty()) ? 1 : 0);
        diary.setSentiment(sentiment);
        diaryRepository.save(diary);

        // Playlist 저장
        Playlist playlist = Playlist.builder()
                .member(member)
                .diary(diary)
                .sentiment(sentiment)
                .artist(artist)
                .song(song)
                .url(url)
                .build();
        playlistRepository.save(playlist);

        return ResponseEntity.ok("일기 업로드 및 추천곡 저장 완료!");
    }
}
