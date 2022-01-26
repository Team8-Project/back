package com.teamproj.backend.Repository.dict;

import com.teamproj.backend.model.dict.DictYoutubeUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DictYoutubeUrlRepository extends JpaRepository<DictYoutubeUrl, Long> {
    List<DictYoutubeUrl> findAllByDict_DictId(Long dictId);
}
