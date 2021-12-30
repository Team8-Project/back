package com.teamproj.backend.Repository.dict;

import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.Dict;
import com.teamproj.backend.model.dict.DictLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DictLikeRepository extends JpaRepository<DictLike, Long> {
    Optional<DictLike> findByUserAndDict(User user, Dict dict);

    boolean existsByUserAndDict(User user, Dict dict);
}
