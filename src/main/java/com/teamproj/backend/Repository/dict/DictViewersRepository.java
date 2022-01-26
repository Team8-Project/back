package com.teamproj.backend.Repository.dict;

import com.teamproj.backend.model.dict.DictViewers;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DictViewersRepository extends JpaRepository<DictViewers, Long> {
    boolean existsByViewerIpAndDict_DictId(String clientIp, Long dictId);
}
