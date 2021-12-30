package com.teamproj.backend.Repository.dict;

import com.teamproj.backend.model.dict.Dict;
import com.teamproj.backend.model.dict.DictViewers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DictViewersRepository extends JpaRepository<DictViewers, Long> {
    boolean existsByViewerIpAndDict(String clientIp, Dict dict);
}
