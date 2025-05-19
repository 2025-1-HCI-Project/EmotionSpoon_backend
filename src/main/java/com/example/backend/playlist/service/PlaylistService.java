package com.example.backend.playlist.service;

import com.example.backend.playlist.dto.PlaylistDTO;
import com.example.backend.playlist.entity.Playlist;
import com.example.backend.playlist.repository.PlaylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;

    public PlaylistDTO getPlaylistByDiaryId(Long diaryId) {
        List<Playlist> playlists = playlistRepository.findAllByDiaryId(diaryId);

        if (playlists.isEmpty()) {
            throw new RuntimeException("플레이리스트가 없습니다.");
        }

        Playlist latest = playlists.stream()
                .max(Comparator.comparing(Playlist::getDate))
                .orElseThrow(() -> new RuntimeException("플레이리스트를 찾을 수 없습니다."));

        return PlaylistDTO.fromEntity(latest);
    }
}
