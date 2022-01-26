package com.teamproj.backend.service.alarm;

import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.Repository.alarm.AlarmRepository;
import com.teamproj.backend.dto.alarm.AlarmNavResponseDto;
import com.teamproj.backend.dto.alarm.AlarmResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.alarm.Alarm;
import com.teamproj.backend.model.alarm.AlarmTypeEnum;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.AlarmService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.teamproj.backend.exception.ExceptionMessages.NOT_EXIST_ALARM;
import static com.teamproj.backend.exception.ExceptionMessages.NOT_YOUR_ALARM;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Transactional
@Rollback
public class AlarmServiceTest {
    @Autowired
    AlarmService alarmService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AlarmRepository alarmRepository;

    User user;
    UserDetailsImpl userDetails;
    Alarm alarm;

    @BeforeEach
    void setup() {
        user = User.builder()
                .username("testuser12345")
                .nickname("1135tester")
                .password("a1234567")
                .build();

        user = userRepository.save(user);
        userDetails = UserDetailsImpl.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();

        alarm = alarmRepository.save(Alarm.builder()
                .navId(65536L)
                .alarmTypeEnum(AlarmTypeEnum.RECEIVE_COMMENT)
                .user(user)
                .checked(false)
                .build());
    }

    @Nested
    @DisplayName("알람 생성")
    class SendAlarm {
        @Test
        @DisplayName("새로운 알람 생성")
        void successNew() {
            // given

            // when
            alarmService.sendAlarm(AlarmTypeEnum.RECEIVE_COMMENT, 65537L, user);

            // then
            Optional<Alarm> result = alarmRepository.findByNavIdAndUser(65537L, user);
            assertTrue(result.isPresent());
        }

        @Test
        @DisplayName("기존 알람 갱신")
        void successExists() {
            // given

            // when
            alarmService.sendAlarm(AlarmTypeEnum.RECEIVE_COMMENT, alarm.getNavId(), user);

            // then
            assertFalse(alarm.isChecked());
        }
    }

    @Nested
    @DisplayName("알람 위치로 이동")
    class NavAlarm {
        @Test
        @DisplayName("성공")
        void navAlarm() {
            // given

            // when
            AlarmNavResponseDto result = alarmService.navAlarm(userDetails, alarm.getAlarmId());

            // then
            assertEquals(result.getNavId(), alarm.getNavId());
        }

        @Nested
        @DisplayName("실패")
        class Fail {
            @Test
            @DisplayName("유효하지 않은 알람에 요청 시도")
            void failNotExistsAlarm() {
                // given

                // when
                Exception exception = assertThrows(NullPointerException.class,
                        () -> alarmService.navAlarm(userDetails, 0L));

                // then
                assertEquals(NOT_EXIST_ALARM, exception.getMessage());
            }

            @Test
            @DisplayName("내 소유가 아닌 알람에 요청 시도")
            void failNotMineAlarm() {
                // given
                User user2 = User.builder()
                        .username("notmyuser1234")
                        .nickname("55315745")
                        .password("a1234567")
                        .build();

                user2 = userRepository.save(user2);

                Alarm otherAlarm = alarmRepository.save(Alarm.builder()
                        .navId(65536L)
                        .alarmTypeEnum(AlarmTypeEnum.RECEIVE_COMMENT)
                        .user(user2)
                        .checked(false)
                        .build());

                // when
                Exception exception = assertThrows(IllegalArgumentException.class,
                        () -> alarmService.navAlarm(userDetails, otherAlarm.getAlarmId()));

                // then
                assertEquals(NOT_YOUR_ALARM, exception.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("알람 읽음 처리")
    class CheckAlarm {
        @Test
        @DisplayName("처리할 알람이 존재하는 경우")
        void existAlarm() {
            // given

            // when
            String result = alarmService.readCheckAllAlarm(userDetails);

            // then
            assertEquals("읽음 처리 완료", result);
        }

        @Test
        @DisplayName("처리할 알람이 존재하지 않는 경우")
        void notExistAlarm() {
            // given
            alarmService.readCheckAllAlarm(userDetails);

            // when
            String result = alarmService.readCheckAllAlarm(userDetails);

            // then
            assertEquals("읽음 처리 완료", result);
        }
    }

    @Nested
    @DisplayName("알람 정보 불러오기")
    class ReceiveAlarm {
        @Test
        @DisplayName("user.isCheck() == true")
        void isCheckIsTrue() {
            // given
            user.setAlarmCheck(true);

            // when
            List<AlarmResponseDto> result = alarmService.receiveAlarm(user);

            // then
            assertEquals(alarm.getAlarmId(), result.get(0).getAlarmId());
        }

        @Nested
        @DisplayName("user.isCheck() == false")
        class CheckIsFalse {
            @Test
            @DisplayName("리스트가 비어있지 않은 경우")
            void isCheckIsFalseListSizeGreaterThan0() {
                // given
                user.setAlarmCheck(false);

                // when
                List<AlarmResponseDto> result = alarmService.receiveAlarm(user);

                // then
                assertEquals(alarm.getAlarmId(), result.get(0).getAlarmId());
            }

            @Test
            @DisplayName("리스트가 비어있는 경우")
            void isCheckIsFalseListSIzeEqual0() {
                // given
                user.setAlarmCheck(false);

                // when
                alarmRepository.deleteById(alarm.getAlarmId());
                List<AlarmResponseDto> result = alarmService.receiveAlarm(user);

                // then
                assertEquals(0, result.size());
            }
        }
    }
}
