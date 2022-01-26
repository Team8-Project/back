package com.teamproj.backend.model.viewers;

import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.util.Timestamped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Viewers extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long viewersId;

    @Column(nullable = false)
    private ViewTypeEnum viewTypeEnum;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false)
    private String viewerIp;
}
