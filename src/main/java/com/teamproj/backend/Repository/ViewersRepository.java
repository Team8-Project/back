package com.teamproj.backend.Repository;

import com.teamproj.backend.model.viewers.ViewTypeEnum;
import com.teamproj.backend.model.viewers.Viewers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ViewersRepository extends JpaRepository<Viewers, Long> {
    Optional<Viewers> findByViewerIpAndViewTypeEnumAndTargetId(String ip, ViewTypeEnum type, Long targetId);
}
