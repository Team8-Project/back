package com.teamproj.backend.model.board;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BoardCategory {
    @Id
    private String categoryName;

    @OneToMany(mappedBy = "boardCategory")
    private List<BoardSubject> boardSubjectList = new ArrayList<>();
}
