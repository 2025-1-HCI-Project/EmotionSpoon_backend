package com.example.backend.diary.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryEventDTO {
    private Long diaryId;
    private String date;
    private String title;
    private String diary;
    private Integer diaryType;
    private String fileName;
    private String sentiment;

    private String song;
    private String artist;
    private String link;
    private Long m_id;
}