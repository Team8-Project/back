package com.teamproj.backend.model.alarm;

import com.teamproj.backend.model.User;
import com.teamproj.backend.util.Timestamped;
import lombok.*;

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
    private boolean checked;

    // 이동해줄 question 위치 받아줄 column 필요함
    // 이런식으로 관리할 땐 데이터 신중하게 컨트롤 해야함!
    @Column(nullable = false)
    private Long navId;

    // 알림 수신 대상자
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
