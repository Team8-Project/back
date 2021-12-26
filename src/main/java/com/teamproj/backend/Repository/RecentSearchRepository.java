package com.teamproj.backend.Repository;

import com.teamproj.backend.model.QueryTypeEnum;
import com.teamproj.backend.model.RecentSearch;
import com.teamproj.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecentSearchRepository extends JpaRepository<RecentSearch, Long> {
    Optional<List<RecentSearch>> findAllByUserAndTypeOrderByCreatedAtDesc(User user, QueryTypeEnum type);
}
