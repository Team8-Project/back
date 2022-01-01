package com.teamproj.backend.util;

import com.querydsl.core.types.Ops;
import com.querydsl.jpa.JPQLTemplates;

// MySQL 의 rand() 기능을 JPQL 상에서 사용할 수 있도록 해주기 위한 클래스.
public class MySqlJpaTemplates extends JPQLTemplates {

    public static final MySqlJpaTemplates DEFAULT = new MySqlJpaTemplates();

    public MySqlJpaTemplates() {
        this(DEFAULT_ESCAPE);
        add(Ops.MathOps.RANDOM, "rand()");
        add(Ops.MathOps.RANDOM2, "rand({0})");
    }

    public MySqlJpaTemplates(char escape) {
        super(escape);
    }
}
