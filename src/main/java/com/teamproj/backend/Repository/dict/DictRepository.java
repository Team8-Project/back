package com.teamproj.backend.Repository.dict;

import com.teamproj.backend.model.dict.Dict;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DictRepository extends JpaRepository<Dict, Long> {
    boolean existsByDictName(String dictName);
}
