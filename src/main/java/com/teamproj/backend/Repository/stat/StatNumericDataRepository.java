package com.teamproj.backend.Repository.stat;

import com.teamproj.backend.model.statistics.StatNumericData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatNumericDataRepository extends JpaRepository<StatNumericData, Long> {
    StatNumericData findByName(String name);
}
