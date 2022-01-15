package com.teamproj.backend.Repository.dict;

import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.Dict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DictRepository extends JpaRepository<Dict, Long> {
    boolean existsByDictName(String dictName);

    Dict findByDictName(String title);

    List<Dict> findByFirstAuthor(User user);

    @Modifying
    @Transactional
    @Query("update Dict d set d.views = d.views + 1 where d.dictId = :id")
    void updateView(Long id);

    Optional<List<Dict>> findAllByDictIdIn(List<Long> idList);

    Page<Dict> findAllByOrderByViewsDesc(Pageable pageable);

    @Query(value = "SELECT *" +
                   "  FROM dict d" +
                   " WHERE match(dict_name, content) against(:q in natural language mode)" +
                   " ORDER BY created_at desc" +
                   " LIMIT :page, :size",
            nativeQuery = true)
    Optional<List<Dict>> findAllByDictNameOrContentByFullText(@Param("q") String query,
                                                     @Param("page") int page,
                                                     @Param("size") int size);
    Long countByCreatedAtGreaterThanEqual(LocalDateTime createdAt);

    Optional<Page<Dict>> findAllByDictNameContainingOrContentContaining(String dictName, String content, Pageable pageable);

    Optional<List<Dict>> findAllByCreatedAtGreaterThanEqual(LocalDateTime localdateTime);
}
