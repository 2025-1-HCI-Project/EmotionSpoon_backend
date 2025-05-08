package com.example.backend.diary.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryDTO {
    private Long memberId;
    private String date;
    private String title;
    private String diaryContent;
}
