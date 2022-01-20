package com.teamproj.backend.Repository.alarm;

import com.teamproj.backend.model.User;
import com.teamproj.backend.model.alarm.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    boolean existsByNavIdAndUser(Long navId, User user);

    Optional<List<Alarm>> findAllByUserAndCheckedOrderByCreatedAtDesc(User user, Boolean isCheck);
    Optional<List<Alarm>> findAllByUserOrderByCreatedAtDesc(User user);

    Optional<Alarm> findByNavIdAndUser(Long navId, User user);
}
