package com.example.backend.diary.controller;

import com.example.backend.diary.dto.DiaryDTO;
import com.example.backend.diary.entity.Diary;
import com.example.backend.diary.repository.DiaryRepository;
import com.example.backend.member.entity.Member;
import com.example.backend.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;

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

        LocalDateTime diaryDate = LocalDateTime.parse(diaryDTO.getDate() + "T00:00:00");

        Diary diary = new Diary();
        diary.setMember(member);
        diary.setDate(diaryDate);
        diary.setContent(diaryDTO.getTitle());
        diary.setDiaryContent(diaryDTO.getDiaryContent());
        diary.setSentiment(null);

        if (file != null && !file.isEmpty()) {
            // 저장 디렉토리 생성
            Path storagePath = Paths.get(diaryDir).toAbsolutePath().normalize();
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }

            // UUID 설정
            String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String storedFileName = UUID.randomUUID() + fileExtension;

            // 파일 저장
            Path filePath = storagePath.resolve(storedFileName);
            file.transferTo(filePath.toFile());

            diary.setFileName(storedFileName);
            diary.setDiaryType(1); // 이미지
        } else {
            diary.setDiaryType(0); // 텍스트
        }

        diaryRepository.save(diary);
        return ResponseEntity.ok("일기 업로드 성공!");
    }
}
