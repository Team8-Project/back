package com.teamproj.backend.Repository.dict;

import com.teamproj.backend.model.dict.Dict;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DictRepository extends JpaRepository<Dict, Long> {
    boolean existsByDictName(String dictName);
}
