package com.teamproj.backend.service.admin;

import com.teamproj.backend.Repository.alarm.AlarmRepository;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.alarm.Alarm;
import com.teamproj.backend.model.alarm.AlarmTypeEnum;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import com.teamproj.backend.util.ValidChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.teamproj.backend.exception.ExceptionMessages.*;

@Service
@RequiredArgsConstructor
public class AlarmService {
    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;

    private final AlarmRepository alarmRepository;

    // 알림 생성
    public void sendAlarm(AlarmTypeEnum alarmTypeEnum, Long navId, User user){
        // 이미 존재하는 알람인지 확인
        if(alarmRepository.existsByNavIdAndUser(navId, user)) {
            // 중복된 알람이면 실행하지 않고 종료.
            return;
        }

        Alarm alarm = Alarm.builder()
                .alarmTypeEnum(alarmTypeEnum)
                .navId(navId)
                .user(user)
                .checked(false)
                .build();

        alarmRepository.save(alarm);
    }

    // 알림 정보 요청
    public void receiveAlarm(UserDetailsImpl userDetails){
        ValidChecker.loginCheck(userDetails);
        User user = jwtAuthenticateProcessor.getUser(userDetails);
        List<Alarm> alarmList = getSafeAlarmListByUser(user);
        // return to Dto List
    }

    // 알림 이동
    @Transactional
    public void navAlarm(UserDetailsImpl userDetails, Long alarmId){
        // 작업 목록
        // 1. 알람 id 확인하여 올바른 정보 전달
        // 2. 알람의 확인여부 true로
        ValidChecker.loginCheck(userDetails);
        User user = jwtAuthenticateProcessor.getUser(userDetails);
        Alarm alarm = getSafeAlarmById(alarmId);

        // 자신의 알람이 아닐 경우 튕겨냄.
        if(user.getId().equals(alarm.getUser().getId())){
            throw new IllegalArgumentException(NOT_YOUR_ALARM);
        }

        // 확인한 알람으로 변경
        alarm.setChecked(true);

        // 알람 위치로(현재는 질문ID만) 이동
        // 질문 아이디만 넘겨주면 됨.
    }

    // Get Safe Entity
    // Alarm
    private Alarm getSafeAlarmById(Long alarmId) {
        Optional<Alarm> alarm = alarmRepository.findById(alarmId);
        return alarm.orElseThrow(() -> new NullPointerException(NOT_EXIST_ALARM));
    }
    // AlarmListByUser
    private List<Alarm> getSafeAlarmListByUser(User user) {
        Optional<List<Alarm>> alarmList = alarmRepository.findAllByUserAndCheckedOrderByCreatedAtDesc(user, false);
        return alarmList.orElseGet(ArrayList::new);
    }
}
