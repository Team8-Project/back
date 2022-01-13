package com.teamproj.backend.util;

import com.querydsl.core.Tuple;

import java.util.HashMap;
import java.util.List;

public class MemegleServiceStaticMethods {
    public static HashMap<String, Boolean> getLikeMap(List<Tuple> likeListTuple) {
        HashMap<String, Boolean> likeMap = new HashMap<>();
        for (Tuple tuple : likeListTuple) {
            // 키값을 "BoardId":"UserId"
            String genString = tuple.get(0, Long.class) + ":" + tuple.get(1, Long.class);
            likeMap.put(genString, true);
        }
        return likeMap;
    }

    public static HashMap<Long, Long> getLikeCountMap(List<Tuple> likeCountListTuple){
        HashMap<Long, Long> likeCountMap = new HashMap<>();
        for (Tuple tuple : likeCountListTuple) {
            // 키값은 DictId, 밸류는 count
            Long key = tuple.get(0, Long.class);
            Long value = tuple.get(1, Long.class);
            likeCountMap.put(key, value);
        }

        return likeCountMap;
    }

    public static HashMap<Long, Long> getLongLongMap(List<Tuple> tupleList) {
        HashMap<Long, Long> map = new HashMap<>();
        for (Tuple tuple : tupleList) {
            Long key = tuple.get(0, Long.class);
            Long value = tuple.get(1, Long.class);
            map.put(key, value);
        }

        return map;
    }
}
