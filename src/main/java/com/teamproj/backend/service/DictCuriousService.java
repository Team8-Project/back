package com.teamproj.backend.service;

import com.teamproj.backend.Repository.dict.DictCuriousRepository;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.DictCurious;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import com.teamproj.backend.util.ValidChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.teamproj.backend.exception.ExceptionMessages.*;

@Service
@RequiredArgsConstructor
public class DictCuriousService {
    private final DictCuriousRepository dictCuriousRepository;
    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;

    public void getDictCurious(){
        List<DictCurious> dictCuriousList = getSafeDictCuriousList();

    }

    public void postDictCurious(UserDetailsImpl userDetails, String curiousName){
        ValidChecker.loginCheck(userDetails);
        User user = jwtAuthenticateProcessor.getUser(userDetails);

        DictCurious dictCurious = dictCuriousRepository.save(DictCurious.builder()
                .curiousName(curiousName)
                .user(user)
                .build());
    }

    public void deleteDictCurious(UserDetailsImpl userDetails, Long dictCuriousId){
        ValidChecker.loginCheck(userDetails);
        DictCurious dictCurious = getSafeDictCurious(dictCuriousId);
        if(!userDetails.getUsername().equals(dictCurious.getUser().getUsername())){
            throw new IllegalArgumentException(NOT_YOUR_CURIOUS);
        }
        dictCuriousRepository.deleteById(dictCurious.getDictCuriousId());
    }

    private DictCurious getSafeDictCurious(Long dictCuriousId) {
        Optional<DictCurious> dictCurious = dictCuriousRepository.findById(dictCuriousId);
        return dictCurious.orElseThrow(() -> new NullPointerException(NOT_EXIST_DICT_CURIOUS));
    }

    private List<DictCurious> getSafeDictCuriousList() {
        // 나중에 옵션 붙을수도 있어서 별도 메소드로 분리해놓음.
        List<DictCurious> dictCuriousList = dictCuriousRepository.findAll();
        return dictCuriousList;
    }
}
