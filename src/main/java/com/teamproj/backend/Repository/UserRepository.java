package com.teamproj.backend.Repository;

import com.teamproj.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByKakaoId(Long kakaoId);

    boolean existsByUsername(String username);

    boolean existsByNickname(String nickname);

    Optional<User> findByNaverId(String id);
}
