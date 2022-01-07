package com.teamproj.backend.model.dict;

import com.teamproj.backend.model.User;
import com.teamproj.backend.util.Timestamped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictHistory extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dictHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Dict dict;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn
    private DictHistory revertFrom;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prevContent;

    @Column(nullable = false)
    private String prevSummary;
}
