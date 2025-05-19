package com.example.backend.playlist.dto;

import com.example.backend.playlist.entity.Playlist;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PlaylistDTO {
    private String sentiment;
    private String artist;
    private String song;
    private String link;
    private LocalDateTime date;
    private String username;

    public static PlaylistDTO fromEntity(Playlist playlist) {
        return PlaylistDTO.builder()
                .sentiment(playlist.getSentiment())
                .artist(playlist.getArtist())
                .song(playlist.getSong())
                .link(playlist.getLink())
                .date(playlist.getDate())
                .username(playlist.getMember().getUsername())
                .build();
    }
}

