package com.example.backend.playlist.controller;

import com.example.backend.playlist.dto.PlaylistDTO;
import com.example.backend.playlist.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/playlist")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @GetMapping("/{diaryId}")
    public ResponseEntity<PlaylistDTO> getPlaylist(@PathVariable Long diaryId) {
        PlaylistDTO dto = playlistService.getPlaylistByDiaryId(diaryId);
        return ResponseEntity.ok(dto);
    }
}
