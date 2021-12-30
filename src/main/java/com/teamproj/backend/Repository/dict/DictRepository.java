package com.teamproj.backend.Repository.dict;

import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.Dict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.util.Streamable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DictRepository extends JpaRepository<Dict, Long> {
    boolean existsByDictName(String dictName);

    Optional<Page<Dict>> findAllByDictNameLikeOrContentLike(String nameQuery, String contentQuery, Pageable pageable);

    Dict findByDictName(String title);

    List<Dict> findByFirstAuthor(User user);

    @Modifying
    @Transactional
    @Query("update Dict d set d.views = d.views + 1 where d.dictId = :id")
    void updateView(Long id);

    Optional<List<Dict>> findAllByDictIdIn(List<Long> idList);

    Page<Dict> findAllByOrderByViewsDesc(Pageable pageable);
}
