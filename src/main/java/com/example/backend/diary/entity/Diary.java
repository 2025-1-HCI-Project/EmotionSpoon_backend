package com.example.backend.diary.entity;

import com.example.backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "diary")
@Getter
@Setter
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "m_id", nullable = false)
    private Member member;

    @Column(length = 512)
    private String content;

    @Column(length = 24)
    private String sentiment;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(length = 126)
    private String fileName;

    //0: 텍스트, 1: 사진
    @Column(nullable = false, columnDefinition = "TINYINT")
    private Integer diaryType;

    @Column(length = 512)
    private String diaryContent;
}
