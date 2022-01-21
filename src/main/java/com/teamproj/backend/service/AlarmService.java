package com.teamproj.backend.service;

import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.Repository.alarm.AlarmRepository;
import com.teamproj.backend.dto.alarm.AlarmNavResponseDto;
import com.teamproj.backend.dto.alarm.AlarmResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.alarm.Alarm;
import com.teamproj.backend.model.alarm.AlarmTypeEnum;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import com.teamproj.backend.util.RedisKey;
import com.teamproj.backend.util.ValidChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.teamproj.backend.exception.ExceptionMessages.NOT_EXIST_ALARM;
import static com.teamproj.backend.exception.ExceptionMessages.NOT_YOUR_ALARM;
import static com.teamproj.backend.util.RedisKey.USER_ALARM_KEY;

@Service
@RequiredArgsConstructor
public class AlarmService {
    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;
    private final RedisTemplate<String, Object> redisTemplate;

    private final RedisService redisService;

    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;

    /*
        알림 송신 지점
        1. 댓글 작성했을 시 질문 작성자에게
        2. 채택 받았을 시 댓글 작성자에게
        3. 채택 받았을 시 나도 궁금해요 입력한 작성자에게
     */
    // 알림 생성
    @Transactional
    public void sendAlarm(AlarmTypeEnum alarmTypeEnum, Long navId, User user) {
        // 이미 존재하는 알람인지 확인
        Alarm alarm = getSafeAlarmByNavIdAndUser(navId, user);
        if (alarm != null) {
            // 중복된 알람이면 다시 활성화만 시키고 종료.
            alarm.setChecked(false);
            return;
        }

        // 중복이 아닐 경우 새로 생성.
        alarm = Alarm.builder()
                .alarmTypeEnum(alarmTypeEnum)
                .navId(navId)
                .user(user)
                .checked(false)
                .build();

        user.setAlarmCheck(false);
        alarmRepository.save(alarm);
        userRepository.save(user);
    }

    private Alarm getSafeAlarmByNavIdAndUser(Long navId, User user) {
        Optional<Alarm> alarm = alarmRepository.findByNavIdAndUser(navId, user);
        return alarm.orElse(null);
    }

    // 알림 정보 요청
    @Transactional
    public List<AlarmResponseDto> receiveAlarm(User user) {
        String redisKey = USER_ALARM_KEY + ":" + user.getId();
        List<AlarmResponseDto> alarmList;

        if (user.isAlarmCheck()) {
            alarmList = redisService.getAlarm(redisKey);
            if (alarmList != null) {
                return alarmList;
            }
        }

        List<Alarm> list = getSafeAlarmListByUser(user);
        alarmList = getAlarmListToResponseDto(list);
        redisService.setAlarm(redisKey, alarmList);

        user.setAlarmCheck(true);
        userRepository.save(user);
        // return to Dto List
        // To do : 우선 List<AlarmResponseDto>에 담아서 리턴 했습니다. 차 후에 수정 필요
        return alarmList;
    }

    // 알림 정보들(AlarmList) Dto에 담아서 리턴
    private List<AlarmResponseDto> getAlarmListToResponseDto(List<Alarm> alarmList) {
        List<AlarmResponseDto> alarmResponseDtoList = new ArrayList<>();

        for (Alarm alarm : alarmList) {
            alarmResponseDtoList.add(AlarmResponseDto.builder()
                    .alarmId(alarm.getAlarmId())
                    .alarmType(alarm.getAlarmTypeEnum().name())
                    .checked(alarm.isChecked())
                    .navId(alarm.getNavId())
                    .username(alarm.getUser().getUsername())
                    .nickname(alarm.getUser().getNickname())
                    .build()
            );
        }
        return alarmResponseDtoList;
    }

    // 알림 이동
    @Transactional
    public AlarmNavResponseDto navAlarm(UserDetailsImpl userDetails, Long alarmId) {
        // 작업 목록
        // 1. 알람 id 확인하여 올바른 정보 전달
        // 2. 알람의 확인여부 true로
        ValidChecker.loginCheck(userDetails);
        User user = jwtAuthenticateProcessor.getUser(userDetails);
        Alarm alarm = getSafeAlarmById(alarmId);

        // 자신의 알람이 아닐 경우 튕겨냄.
        if (!user.getId().equals(alarm.getUser().getId())) {
            throw new IllegalArgumentException(NOT_YOUR_ALARM);
        }

        // 확인한 알람으로 변경
        alarm.setChecked(true);

        // 알람 위치로(현재는 질문ID만) 이동
        // 질문 아이디(navId)만 넘겨주는 DTO 작성하면 됨.
        // 이후 알람 종류가 늘어날 경우 Enum값도 같이 넘겨줘야 함.
        return AlarmNavResponseDto.builder()
                .navId(alarm.getNavId())
                .build();
    }

    // region 알림 읽음으로 처리
    @Transactional
    public String readCheckAlarm(Long alarmId, UserDetailsImpl userDetails) {
        // 알람 아이디로 알람 정보 가져오기
        Alarm alarm = getSafeAlarmById(alarmId);
        User user = jwtAuthenticateProcessor.getUser(userDetails);
        // 자신의 알람이 아닐 경우 튕겨냄.
        if (!user.getId().equals(alarm.getUser().getId())) {
            throw new IllegalArgumentException(NOT_YOUR_ALARM);
        }
        // 알람 읽음 처리
        alarm.setChecked(true);
        // 읽음 처리 완료 메시지 Response
        return "읽음 처리 완료";
    }
    // endregion

    // region 모든 알림 읽음으로 처리
    @Transactional
    public String readCheckAllAlarm(UserDetailsImpl userDetails) {
        // 로그인 체크
        ValidChecker.loginCheck(userDetails);
        // 알람 아이디로 알람 정보 가져오기
        User user = jwtAuthenticateProcessor.getUser(userDetails);
        // 알람 읽음 처리
        List<Alarm> alarmList = getSafeAlarmListByUserNotRead(user);
        for (Alarm alarm : alarmList) {
            alarm.setChecked(true);
        }
        redisTemplate.delete(USER_ALARM_KEY+":"+user.getId());
        // 읽음 처리 완료 메시지 Response
        return "읽음 처리 완료";
    }
    // endregion

    // Get Safe Entity
    // Alarm
    private Alarm getSafeAlarmById(Long alarmId) {
        Optional<Alarm> alarm = alarmRepository.findById(alarmId);
        return alarm.orElseThrow(() -> new NullPointerException(NOT_EXIST_ALARM));
    }

    // AlarmListByUser
    private List<Alarm> getSafeAlarmListByUser(User user) {
        Optional<List<Alarm>> alarmList = alarmRepository.findAllByUserOrderByCreatedAtDesc(user);
        return alarmList.orElseGet(ArrayList::new);
    }

    // AlarmListByUserNotRead
    private List<Alarm> getSafeAlarmListByUserNotRead(User user) {
        Optional<List<Alarm>> alarmList = alarmRepository.findAllByUserAndCheckedOrderByCreatedAtDesc(user, false);
        return alarmList.orElseGet(ArrayList::new);
    }
}
