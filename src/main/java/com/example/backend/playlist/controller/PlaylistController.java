package com.example.backend.playlist.controller;

import com.example.backend.playlist.dto.PlaylistDTO;
import com.example.backend.playlist.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/playlist")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @GetMapping("/{diaryId}")
    public ResponseEntity<List<PlaylistDTO>> getPlaylist(@PathVariable Long diaryId) {
        List<PlaylistDTO> dto = playlistService.getPlaylistByDiaryId(diaryId);
        return ResponseEntity.ok(dto);
    }
}
