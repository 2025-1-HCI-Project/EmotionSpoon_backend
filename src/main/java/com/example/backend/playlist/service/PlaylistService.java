package com.example.backend.playlist.service;

import com.example.backend.playlist.dto.PlaylistDTO;
import com.example.backend.playlist.entity.Playlist;
import com.example.backend.playlist.repository.PlaylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;

    public List<PlaylistDTO> getPlaylistByDiaryId(Long diaryId) {
        List<Playlist> playlists = playlistRepository.findAllByDiaryId(diaryId);

        if (playlists.isEmpty()) {
            throw new RuntimeException("플레이리스트가 없습니다.");
        }

        return playlists.stream()
                .map(PlaylistDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
