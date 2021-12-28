package com.teamproj.backend.Repository.stat;

import com.teamproj.backend.model.statistics.StatVisitor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatVisitorRepository extends JpaRepository<StatVisitor, Long> {
    Optional<StatVisitor> findByVisitorIp(String clientIp);
}
