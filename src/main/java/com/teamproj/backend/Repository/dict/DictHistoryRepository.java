package com.teamproj.backend.Repository.dict;

import com.teamproj.backend.model.dict.Dict;
import com.teamproj.backend.model.dict.DictHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DictHistoryRepository extends JpaRepository<DictHistory, Long> {
    Optional<List<DictHistory>> findAllByDictOrderByCreatedAtDesc(Dict dict);
}
