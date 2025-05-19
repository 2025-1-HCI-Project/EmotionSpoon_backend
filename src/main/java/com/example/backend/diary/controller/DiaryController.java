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

    @PostMapping("/save")
    @Transactional
    public ResponseEntity<?> saveDiary(
            @RequestPart("dto") DiaryDTO diaryDTO,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        Member member = memberRepository.findById(diaryDTO.getMemberId())
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        String storedFileName = null;
        if (file != null && !file.isEmpty()) {
            Path storagePath = Paths.get(diaryDir).toAbsolutePath().normalize();
            if (!Files.exists(storagePath)) Files.createDirectories(storagePath);

            String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            storedFileName = UUID.randomUUID() + fileExtension;
            Path filePath = storagePath.resolve(storedFileName);
            file.transferTo(filePath.toFile());
        }

        LocalDateTime diaryDate = LocalDateTime.parse(diaryDTO.getDate() + "T00:00:00");
        Diary diary = new Diary();
        diary.setMember(member);
        diary.setDate(diaryDate);
        diary.setContent(diaryDTO.getTitle());
        diary.setDiaryContent(diaryDTO.getDiaryContent());
        diary.setFileName(storedFileName);
        diary.setDiaryType((file != null && !file.isEmpty()) ? 1 : 0);

        diaryRepository.save(diary);

        return ResponseEntity.ok(Map.of("diaryId", diary.getId()));
    }


    @PostMapping("/analyze")
    @Transactional
    public ResponseEntity<?> analyzeDiary(@RequestBody Map<String, Object> requestBody) {
        Object idObj = requestBody.get("id");

        if (idObj == null) {
            return ResponseEntity.badRequest().body("일기 ID가 없습니다.");
        }

        Long id = Long.parseLong(idObj.toString());

        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("일기 없음"));

        String fullText = diary.getDiaryContent();
        if (diary.getFileName() != null) {
            Path path = Paths.get(diaryDir, diary.getFileName());
            try {
                byte[] fileBytes = Files.readAllBytes(path);

                HttpHeaders imgHeaders = new HttpHeaders();
                imgHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

                ByteArrayResource resource = new ByteArrayResource(fileBytes) {
                    @Override
                    public String getFilename() {
                        return diary.getFileName();
                    }
                };

                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("file", resource);
                HttpEntity<MultiValueMap<String, Object>> imageRequest = new HttpEntity<>(body, imgHeaders);

                ResponseEntity<Map> ocrRes = new RestTemplate().postForEntity("http://localhost:5050/ocr", imageRequest, Map.class);
                fullText = (String) ocrRes.getBody().get("text");

            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("OCR 실패: " + e.getMessage());
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(Map.of("input_data", fullText), headers);

        ResponseEntity<Map> res = new RestTemplate().postForEntity("http://localhost:5050/recommend", request, Map.class);

        String sentiment = (String) res.getBody().get("sentiment");
        String artist = (String) res.getBody().get("artist");
        String song = (String) res.getBody().get("song");
        String url = (String) res.getBody().get("link");

        diary.setSentiment(sentiment);
        diaryRepository.save(diary);

        Playlist playlist = Playlist.builder()
                .member(diary.getMember())
                .diary(diary)
                .sentiment(sentiment)
                .artist(artist)
                .song(song)
                .url(url)
                .build();
        playlistRepository.save(playlist);

        return ResponseEntity.ok("분석 및 추천 저장 완료");
    }
}
