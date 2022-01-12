package com.teamproj.backend.model.alarm;

import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.Dict;
import com.teamproj.backend.util.Timestamped;
import lombok.*;
import org.springframework.stereotype.Service;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Alarm extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alarmId;

    @Column(nullable = false)
    private AlarmTypeEnum alarmTypeEnum;

    @Column(nullable = false)
    private boolean isCheck;

    // 이동해줄 question 위치 받아줄 column 필요함

    // 알림 수신 대상자
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
