package com.teamproj.backend.Repository.dict;

import com.teamproj.backend.model.dict.Dict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DictRepository extends JpaRepository<Dict, Long> {
    boolean existsByDictName(String dictName);

    Optional<Page<Dict>> findAllByDictNameLikeOrContentLike(String nameQuery, String contentQuery, Pageable pageable);
}
