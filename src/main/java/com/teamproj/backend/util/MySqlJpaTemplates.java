package com.teamproj.backend.util;

import com.querydsl.core.types.Ops;
import com.querydsl.jpa.JPQLTemplates;

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
