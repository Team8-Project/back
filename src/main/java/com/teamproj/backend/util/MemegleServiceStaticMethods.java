package com.teamproj.backend.util;

import com.querydsl.core.Tuple;

import java.util.HashMap;
import java.util.List;

public class MemegleServiceStaticMethods {
    public static HashMap<String, String> getUserInfoMap(List<Tuple> tupleList){
        HashMap<String, String> userInfoMap = new HashMap<>();
        for (Tuple tuple : tupleList) {
            // Long key : boardId
            Long key = tuple.get(0, Long.class);
            // 키값은 boardId:username, 밸류는 username
            String username = tuple.get(1, String.class);
            String usernameKey = key + ":username";
            userInfoMap.put(usernameKey, username);
            // 키값은 boardId:nickname, 밸류는 nickname
            String nickname = tuple.get(2, String.class);
            String nicknameKey = key + ":nickname";
            userInfoMap.put(nicknameKey, nickname);
            // 키값은 boardId:profileImage, 밸류는 profileImage
            String profileImageKey = key + ":profileImage";
            String profileImage = tuple.get(3, String.class);
            userInfoMap.put(profileImageKey, profileImage);
        }

        return userInfoMap;
    }

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
