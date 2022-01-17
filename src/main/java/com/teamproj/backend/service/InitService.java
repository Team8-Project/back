package com.teamproj.backend.service;

import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.Repository.quiz.QuizBankRepository;
import com.teamproj.backend.Repository.quiz.QuizRepository;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.Dict;
import com.teamproj.backend.model.quiz.Quiz;
import com.teamproj.backend.model.quiz.QuizBank;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import com.teamproj.backend.util.ValidChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InitService {
    // Quiz
    private final QuizRepository quizRepository;
    private final QuizBankRepository quizBankRepository;

    /*
        퀴즈 후보 열람
        쉬움(밈기적)
        - 난 ㄱ ㅏ 끔...
        - 나는 지금 미쳐가고 있다
        - 엄청 큰 모기가 나의 발을 물었어
        - 간 때문이야
        - 박수쳐
        - 펀쿨섹
        - 지디?
        - 자기야 왜 또 칭얼거려
        - 멈춰!
        - 무야호

        보통(밈잘알)
        - 블랙워그레이몬
        - 인성문제있어
        - 수도꼭지
        - 에스파는 나야
        - 야, 이기영!
        - 형이 왜 거기서 나와
        - 빅맥
        - 아이돌 유행어
        - 지상렬
        - 요리왕비룡

        어려움(밈중독)
        - 군침싹
        - 없어요
        - 나비보벳따우
        - 다메다네
        - 대기
        - 스고이재팬
        - 펀쿨흡족
        - 원남쓰
        - 2000년
        - 심영
     */
    public void initQuiz() {
        // 퀴즈리스트(손으로넣음)
        List<String> quizImgList = new ArrayList<>();
        List<String> questionList = new ArrayList<>();
        List<String> choiceList = new ArrayList<>();

        // 쉬움
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/sosad.png"); // ㄱ ㅏ 끔...
//        questionList.add("LV1#위 사진 속 인물과 가장 관련이 깊은 단어는 무엇인가?");
//        choiceList.add("두뇌 풀가동");
//        choiceList.add("김치 싸대기");
//        choiceList.add("섹도시발");
//        choiceList.add("별빛로긔_☆");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/liver.gif"); // 간때문이야
//        questionList.add("LV1#어떤 제품과 관련된 영상인가?");
//        choiceList.add("우루사");
//        choiceList.add("인사돌");
//        choiceList.add("이가탄");
//        choiceList.add("아로나민");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/funcool.png"); // 펀쿨섹
//        questionList.add("LV1#이 사람이 실제로 하지 않은 말은?");
//        choiceList.add("지금의 일본이 바뀌기 위해서는, 변화 해야 합니다.");
//        choiceList.add("기후변화같은 큰 문제를 다룰 땐 Fun해야 합니다. 그리고 Cool하고 Sexy하게 대처해야 합니다.");
//        choiceList.add("반성해야 한다고 생각하고 반성하고 있는데 반성하고 있다는 자세를 보이고 있지 못 하다는 점에서 반성을 해야한다고 생각합니다.");
//        choiceList.add("지금처럼이면 안 된다고 생각합니다. 그렇기 때문에 일본은 지금처럼이면 안 된다고 생각합니다.");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/crazy.jpg"); // 나는 지금 미쳐가고있다
//        questionList.add("LV1#빈 칸에 들어갈 말은?");
//        choiceList.add("음악만이 나라에서 허락하는 유일한 마약이니까");
//        choiceList.add("음악 없는 시간 속의 나는 감당할 수 없으니까");
//        choiceList.add("음악만이 나를 가장 화려하게 빛내니까");
//        choiceList.add("음악은 나를 자유롭게 해주니까");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/eunkwang.jpeg"); // 소리지르지말고
//        questionList.add("LV1#서은광이 콘서트 중 외친 말은?");
//        choiceList.add("모두 소리지르지 말고~...박수쳐!");
//        choiceList.add("소리질러!");
//        choiceList.add("에블바디 스탠드 업~!");
//        choiceList.add("순천 렛츠기릿!!!");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/gaeko.png"); // GD?
//        questionList.add("LV1#개코가 이 상황에서 한 말은?");
//        choiceList.add("GD?");
//        choiceList.add("나왔다.");
//        choiceList.add("자이언티?");
//        choiceList.add("나 진짜 형인 줄 알았어");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/honey.png"); // 자기야 왜 칭얼거려
//        questionList.add("LV1#사진 속 인물이 당신에게 속삭일 말은?");
//        choiceList.add("자기야. 왜 또 칭얼거려.");
//        choiceList.add("이 몸, 등장.");
//        choiceList.add("잇힝");
//        choiceList.add("어, 예쁘다.");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/myfeet.gif"); // 나의 발을 모기가 물었어
//        questionList.add("LV1#어떤 주제를 다룬 춤인가?");
//        choiceList.add("엄청 큰 모기가 나의 발을 물었어");
//        choiceList.add("사마귀가 사냥을 시작했어");
//        choiceList.add("이걸 살까 저걸 살까 둘다 골랐어");
//        choiceList.add("종묘제례악");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/stop.gif"); // 멈춰
//        questionList.add("LV1#유행어 '멈춰!'와 관계 없는 것은?");
//        choiceList.add("과속");
//        choiceList.add("뉴스");
//        choiceList.add("노르웨이");
//        choiceList.add("학교폭력");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/yaho.gif"); // 무야호
//        questionList.add("LV1#이 장면 이후, '그만큼 신나시다는거지~'라고 말한 사람은 누구인가?");
//        choiceList.add("정형돈");
//        choiceList.add("유재석");
//        choiceList.add("노홍철");
//        choiceList.add("정준하");

        /*
         ********************************************************************************************************
         */

        // 보통
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/blackwar.jpg"); // 블랙워그레이몬
//        questionList.add("LV2#빈 칸에 들어갈 문장은?");
//        choiceList.add("나는 나보다 약한 녀석의 명령 따위는 듣지 않는다");
//        choiceList.add("그런 걸 말이라고 하는거냐 멍청한 놈");
//        choiceList.add("그저 버러지의 단말마로 들리는 군");
//        choiceList.add("너 따위가 나에게 명령할 권한은 없다");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/emergency.jpg"); // 비상이다
//        questionList.add("LV2#글쓴이가 마지막에 한 말은?");
//        choiceList.add("비상이다");
//        choiceList.add("재난이다");
//        choiceList.add("응급이다");
//        choiceList.add("큰일이다");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/hey.png"); // 여름이었다
//        questionList.add("LV2#“야, 이기영!” ");
//        choiceList.add("여름이었다.");
//        choiceList.add("봄이었다.");
//        choiceList.add("가을이었다.");
//        choiceList.add("겨울이었다.");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/insung.jpg"); // 인성문제있어
//        questionList.add("LV2#사진 속 상황에 대한 설명으로 알맞은 것은?");
//        choiceList.add("훈련병이 훈련을 수행하던 중 바닥에 침을 뱉었다.");
//        choiceList.add("훈련병이 목봉 체조 중 몸을 숙여 혼자 휴식을 취했다.");
//        choiceList.add("훈련병이 번호를 외치던 중 여섯이 아닌 버섯이라고 말했다.");
//        choiceList.add("훈련병이 지혈 훈련 중 ‘f**k’ 이라고 외쳤다.");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/espa.jpg"); // 에스파는 나야
//        questionList.add("LV2#다음 이미지에 같이 쓰인 트윗은?");
//        choiceList.add("에스파는 나야 둘이 될 수 없어");
//        choiceList.add("말티즈는 나야 주인님 그녀석은 가짜에요");
//        choiceList.add("에스파는 나야 둘이 될...수 있네?");
//        choiceList.add("말티즈는 나야 둘이 될 수 없어");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/bro.jpg"); // 형이 거기서 왜 나와
//        questionList.add("LV2#무한도전 속 위 장면에 해당하는 에피소드와 상황을 바르게 짝지어 설명한 것은?");
//        choiceList.add("유혹의 거인 - 술을 먹던 중 갑작스러운 유재석의 등장");
//        choiceList.add("WM7 - 연습을 하던 중 대본에 없던 유재석의 난입");
//        choiceList.add("퍼펙트센스 - 헬기 몰래카메라를 마친 후 유재석이 등장");
//        choiceList.add("의상한 형제 - 최후의 순간 정형돈의 집 앞에 등장한 동맹 유재석");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/bigmac.jpg"); // 빅맥
//        questionList.add("LV2#빅맥송에서 말하는 빅맥 구성요소의 순서로 옳은 것은?");
//        choiceList.add("참깨빵-패티-소스-양상추-치즈-피클-양파");
//        choiceList.add("참깨빵-양상추-패티-소스-치즈-피클-양파");
//        choiceList.add("참깨빵-패티-치즈-양상추-소스-피클-양파");
//        choiceList.add("참깨빵-양상추-치즈-패티-소스-피클-양파");

//        quizImgList.add(""); // 아이돌 유행어
//        questionList.add("LV2#다음 중, 유행어의 어원이 아이돌이 아닌 것은 무엇인가?");
//        choiceList.add("뻘쭘하다");
//        choiceList.add("완전 ~하다");
//        choiceList.add("깜놀");
//        choiceList.add("1도 모르겠다");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/jisang.jpg"); // 지상렬
//        questionList.add("LV2#위 사진 속 인물이 실제로 하지 않은 말은?");
//        choiceList.add("이 사람이 나에게 디스어포인트를 준다");
//        choiceList.add("혓바닥에서 와이파이좀 터진다");
//        choiceList.add("안구에 습기가 찼다");
//        choiceList.add("식도에 조명탄이 터진다");
//
        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/mimi.gif"); // 미미
        questionList.add("LV2#영상 속 인물이 내린 평가로 올바른 것은?");
        choiceList.add("지복(至福)");
        choiceList.add("미미(美味)");
        choiceList.add("초절(超絶)");
        choiceList.add("극락(極樂)");

        /*
         ********************************************************************************************************
         */

        // 어려움
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/cute.jpg"); // 군침
//        questionList.add("LV3#위 사진 속 가려진 단어와 가장 관련이 깊은 것은?");
//        choiceList.add("뽀롱뽀롱 뽀로로");
//        choiceList.add("히어로즈 오브 더 스톰");
//        choiceList.add("속지마 주인님 저 녀석은 가짜야");
//        choiceList.add("내 오른손에 흑염룡이 잠들어 있다");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/nothing.gif"); // 없어요
//        questionList.add("LV3#마지막에 들어갈 말은?");
//        choiceList.add("아니 없어요 그냥");
//        choiceList.add("아니 그런거 없어요");
//        choiceList.add("원래 없었어요");
//        choiceList.add("아니요 없어요");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/navibo.png"); // 나비보벳따우우
//        questionList.add("LV3#나비보벳따우로 유명한 이 캐릭터의 이름은 무엇일까?");
//        choiceList.add("K.K");
//        choiceList.add("J.J");
//        choiceList.add("T.P");
//        choiceList.add("A.J");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/damedane.gif"); // 다메다네
//        questionList.add("LV3#‘다메다네’ 로 유명한 게임 ‘용과 같이’의 삽입곡의 곡명은 무엇일까?");
//        choiceList.add("바보같아(ばかみたい)");
//        choiceList.add("안 되겠네(だめだね)");
//        choiceList.add("일그러지지 않는 추억(歪まない思い)");
//        choiceList.add("오직 당신만 믿고서(あんた信じるばかりで)");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/degi.jpg"); // 박대기
//        questionList.add("LV3#위 사진 속 기자의 이메일 주소로 옳은 것은?");
//        choiceList.add("waiting@kbs.co.kr");
//        choiceList.add("weather@kbs.co.kr");
//        choiceList.add("air@kbs.co.kr");
//        choiceList.add("standby@kbs.co.kr");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/sugoi.gif"); // 스고이재팬
//        questionList.add("LV3#위 장면은 일본의 프로그램 ‘메이드 인 재팬’의 일부분이다. 이 장면이 등장한 에피소드의 주제로 옳은 것은?");
//        choiceList.add("일본의 [택시 자동문]이 칠레에 첫 상륙");
//        choiceList.add("일본의 [식품 배달 드론]이 칠레에 첫 상륙");
//        choiceList.add("일본의 [가츠동과 카레]가 칠레에 첫 상륙");
//        choiceList.add("일본의 [초고속 열차]가 칠레에 첫 상륙");

//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/funcoolok.gif"); // 펀쿨흡족
//        questionList.add("LV3#해당 인물은 어떤 발언을 한 뒤 흡족한 웃음을 지었을까?");
//        choiceList.add("하겠습니다. 그것이 약속이니까.");
//        choiceList.add("기후변화같은 큰 문제를 다룰 땐 Fun해야 합니다. 그리고 Cool하고 Sexy하게 대처해야 합니다.");
//        choiceList.add("촌스러운 설명은 필요 없을 것 같네요.");
//        choiceList.add("실루엣이 떠올랐습니다. 어렴풋이 떠올랐어요, 46이라는 숫자가요.");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/wonnam.png"); // 원남쓰
//        questionList.add("LV3#여기서, 원남쓰의 나이는 몇 살일까?");
//        choiceList.add("30살");
//        choiceList.add("23살");
//        choiceList.add("26살");
//        choiceList.add("34살");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/2000.PNG"); // 2000년
//        questionList.add("LV3#다음 중, 2000년대에 탄생한 캐릭터가 아닌 것은 무엇인가?");
//        choiceList.add("펩시맨");
//        choiceList.add("뿌까");
//        choiceList.add("우비소년");
//        choiceList.add("마시마로");
//
//        quizImgList.add("https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/quiz/2000.PNG"); // 2000년
//        questionList.add("LV3#다음 상황에 이어지는 장면은 무엇인가?");
//        choiceList.add("김두한 일행이 심영을 살해하려고 한다.");
//        choiceList.add("의사가 심영이 고자가 되었음을 알린다.");
//        choiceList.add("수도경찰서의 형사가 조사를 위해 찾아온다.");
//        choiceList.add("김두한 일행이 찾아온다.");

        List<Quiz> quizList = new ArrayList<>();
        for (int i = 0; i < questionList.size(); i++) {
            String[] question = questionList.get(i).split("#");
            quizList.add(Quiz.builder()
                    .quizImage(quizImgList.get(i))
                    .question(question[1])
                    .category(question[0])
                    .build());
        }

        List<Quiz> saveQuizList = quizRepository.saveAll(quizList);
        List<QuizBank> quizBankList = new ArrayList<>();

        for (int i = 0; i < questionList.size(); i++) {
            for (int k = 0; k < 4; k++) {
                int choiceIdx = i * 4;
                quizBankList.add(QuizBank.builder()
                        .choice(choiceList.get(choiceIdx + k))
                        .quiz(saveQuizList.get(i))
                        .build());
            }
        }

        List<QuizBank> saveQuizBankList = quizBankRepository.saveAll(quizBankList);
        for (int i = 0; i < questionList.size(); i++) {
            Quiz quiz = saveQuizList.get(i);
            quiz.setSolution(saveQuizBankList.get(i * 4));
            quizRepository.save(quiz);
        }
    }

    // Dict
    private final DictRepository dictRepository;
    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;

    public void initDict(UserDetailsImpl userDetails) {
        ValidChecker.loginCheck(userDetails);

        // 사전리스트(손으로.. 넣음..)
        List<String> titleList = new ArrayList<>();
        List<String> summaryList = new ArrayList<>();
        List<String> contentList = new ArrayList<>();
        titleList.add("700");
        summaryList.add("'귀여워'를 숫자로 표현한 단어");
        contentList.add("귀여워의 초성인 ㄱㅇㅇ를 숫자로 표현한 단어.");
        titleList.add("갓생");
        summaryList.add("부지런한 삶을 의미함.");
        contentList.add("God과 인생이 합쳐진 단어로, 부지런한 삶을 의미한다. 아이돌 팬덤 사이에서 덕질에 과몰입하는 것을 멈추고 현실 생활에 집중하겠다는 의미로 사용되다가 이후 의미가 확장되었다.");
        titleList.add("고독방 / 고독한 방");
        summaryList.add("주제에 대한 이미지만 공유하는 오픈채팅방.");
        contentList.add("안고독방의 반댓말로, 일반 채팅을 금지하고 특정 주제에 대한 이미지만 공유하는 카카오톡 오픈채팅방을 의미한다.");
        titleList.add("광공 / 집착광공");
        summaryList.add("특정 대상에게 집착하는 캐릭터.");
        contentList.add("미칠 광(狂)과 칠 공(攻)을 합친 단어로, 특정 대상에게 집착하는 캐릭터를 의미한다. 남성의 동성 로맨스(BL : Boys Love)에서 생겨난 단어이다.");
        titleList.add("구독계");
        summaryList.add("글을 보기 위한 목적의 트위터 계정.");
        contentList.add("직접 활동하는 것이 아닌, 상대방이 쓰는 글을 보거나 리트윗만 하기 위해 이용하는 트위터 계정.");
        titleList.add("국그릇핑크퐁");
        summaryList.add("'ㅋㅋ루삥뽕'의 변형.");
        contentList.add("스트리밍 플랫폼 트위치에서 파생된 유행어인 'ㅋㅋ루삥뽕'을 변형해서 쓰는 말.");
        titleList.add("다꾸");
        summaryList.add("다이어리 꾸미기의 준말.");
        contentList.add("다이어리 꾸미기의 준말. 파생어로 다이어리 꾸미기를 취미로 가진 사람을 의미하는 '다꾸족' '다꾸러'가 있다.");
        titleList.add("댈컴");
        summaryList.add("대리 커스텀의 준말.");
        contentList.add("대리 커스텀의 준말. 게임이나 메타버스플랫폼에서 아바타 꾸미기를 타인에게 맡기는 것을 의미한다.");
        titleList.add("디깅");
        summaryList.add("흥미 있는 분야의 정보를 검색하는 것.");
        contentList.add("채굴, 발굴을 뜻하는 단어인 디깅(Digging)으로, 흥미 있는 분야의 정보를 얻기 위해 이것저것 검색해보는 일을 의미한다.");
        titleList.add("떡메");
        summaryList.add("떡제본된 메모지를 뜻함.");
        contentList.add("메모지 귀퉁이 끝부분만 붙여 제본된(떡제본된) 메모지를 뜻한다.");
        titleList.add("랜봉");
        summaryList.add("상품 목록을 랜덤하게 제공하는 것.");
        contentList.add("랜덤 봉투의 준말. 판매하는 상품 목록을 랜덤하게 담아서 제공하는 것을 의미한다.");
        titleList.add("린스타");
        summaryList.add("전체 공개 인스타그램 계정을 의미함.");
        contentList.add("진짜 인스타(Read Instagram)의 준말. 전체 공개로 공유하고 싶은 일상을 올리는 인스타그램 계정을 의미한다.");
        titleList.add("~멍");
        summaryList.add("멍을 때리며 휴식을 취하는 것.");
        contentList.add("멍을 때리며 휴식을 취하는 것을 뜻한다. 불을 보며 멍을 때리는 불멍, 물을 보며 멍을 때리는 물멍, 그 외 산멍 등으로 파생된다.");
        titleList.add("밥약");
        summaryList.add("밥 약속의 준말.");
        contentList.add("밥 약속의 준말.");
        titleList.add("모버실");
        summaryList.add("모든 버전 실시간의 준말.");
        contentList.add("모든 버전 실시간의 준말. 일반 영상을 실제 채팅이 올라오는 것처럼 편집한 영상을 의미한다.");
        titleList.add("뽀시래기");
        summaryList.add("작고 귀여운 동물이나 사람을 뜻함.");
        contentList.add("'부스러기'의 전라도와 경상도 방언. 작고 귀여운 동물이나 사람을 가리킬 때 쓴다.");
        titleList.add("비상이다");
        summaryList.add("눈물이 날 것 같은 상황.");
        contentList.add("한 익명 게시판에서 유래한 단어로, 눈물이 날 것 같은 상황을 의미한다.");
        titleList.add("손민수");
        summaryList.add("누군가를 따라하는 것.");
        contentList.add("누군가를 따라하는 것을 의미하는 단어로, 웹툰 '치즈인더트랩'의 캐릭터 손민수의 주인공을 따라하는 행동에서 유래되었다.");
        titleList.add("스꾸");
        summaryList.add("스티커 꾸미기의 준말.");
        contentList.add("스티커 꾸미기의 준말로, 다이어리 등을 꾸미는 것을 의미한다.");
        titleList.add("스카");
        summaryList.add("스터디 카페의 준말.");
        contentList.add("스터디 카페의 준말.");
        titleList.add("안고독방 / 안 고독한 방");
        summaryList.add("주제에 대한 채팅도 가능한 오픈채팅방.");
        contentList.add("고독방의 반댓말로, 대화도 하며 주제에 대한 이미지도 공유하는 카카오톡 오픈 채팅방을 의미한다.");
        titleList.add("알잘딱깔센");
        summaryList.add("알아서 잘 딱 깔끔하고 센스있게.");
        contentList.add("알아서 잘 딱 깔끔하고 센스있게.");
        titleList.add("어쩔티비");
        summaryList.add("'어쩌라고'의 의미를 가진 유행어.");
        contentList.add("'어쩌라고'라는 의미를 가진 유행어로, '어쩌라고, TV나 봐' 또는 유튜브 등에서 10대들이 짓는 채널명 'ㅇㅇTV'에서 유래한 것으로 추정되고 있다. 저쩔세탁기, 어쩔프리미엄다이슨청소기, 어쩔1.8리터대용량초고속가정용다용도믹서기 등 각종 전자제품들을 뒤에 붙이는 식으로 파생된다.");
        titleList.add("옾챗");
        summaryList.add("카카오톡 오픈채팅방의 준말.");
        contentList.add("카카오톡 오픈채팅방의 준말.");
        titleList.add("인스");
        summaryList.add("인쇄 스티커의 준말.");
        contentList.add("인쇄 스티커의 준말로, 다이어리를 꾸밀 때 사용된다.");
        titleList.add("인스스");
        summaryList.add("인스타그램 스토리의 준말.");
        contentList.add("인스타그램 스토리의 준말.");
        titleList.add("ㅈㅂㅈㅇ");
        summaryList.add("정보좀요의 초성.");
        contentList.add("'정보좀요'의 초성을 따 만들어진 용어.");
        titleList.add("재질");
        summaryList.add("무언가에 대한 느낌.");
        contentList.add("무언가에 대한 느낌을 포괄적으로 이르는 신세대 용어. ex) 스타일 완전 내 재질");
        titleList.add("코시국");
        summaryList.add("코로나 시국의 준말.");
        contentList.add("코로나 시국의 준말.");
        titleList.add("킹리적 갓심");
        summaryList.add("합리적 의심.");
        contentList.add("합리적 의심에 킹(King)과 갓(God)을 붙여 만든 신조어.");
        titleList.add("킹받네");
        summaryList.add("열받는다는 뜻.");
        contentList.add("킹(King) + 열받는다 의 합성어. 열받지 않는 상황에도 자주 사용한다.");
        titleList.add("킹정");
        summaryList.add("강하게 인정한다는 뜻.");
        contentList.add("킹(King) + 인정 의 합성어. 강하게 인정할 때 사용한다.");
        titleList.add("탭꾸");
        summaryList.add("태블릿 꾸미기의 준말.");
        contentList.add("태블릿 꾸미기의 준말로, 태블릿 PC의 화면을 꾸미는 것을 뜻한다.");
        titleList.add("폰꾸");
        summaryList.add("휴대폰 꾸미기의 준말.");
        contentList.add("폰 꾸미기의 준말로, 휴대폰 화면을 커스텀 하는 것을 뜻한다.");
        titleList.add("프사");
        summaryList.add("프로필 사진의 준말.");
        contentList.add("프로필 사진의 준말.");
        titleList.add("핀스타");
        summaryList.add("비공개 인스타그램 계정을 의미함.");
        contentList.add("린스타의 반댓말로, 가짜 인스타그램(Fake Instagram)의 준말. 비공개 계정으로 설정해 린스타와 다른 일상을 공유하는 계정을 의미한다.");
        titleList.add("힙하다");
        summaryList.add("새롭고 개성 있는 것을 뜻함.");
        contentList.add("힙스터스럽다의 준말. 트렌디하다는 단어와 유사한 의미로, 새롭고 개성 있는 것을 의미한다.");
        titleList.add("갓기");
        summaryList.add("어린 천재.");
        contentList.add("신을 뜻하는 갓(god)과 아기의 합성어로 뛰어난 실력을 갖춘 어린 천재를 지칭한다.");
        titleList.add("몰?루");
        summaryList.add("모른다는 뜻.");
        contentList.add("모른다는 뜻으로, 최초는 디시인사이드 메이플스토리 갤러리에서 유래되었다고 전해진다. 넥슨의 모바일게임 '블루아카이브'를 통해 유명해졌다.");
        titleList.add("머선129");
        summaryList.add("무슨 일이야?");
        contentList.add("‘무슨 일이야?’를 경상도 사투리로 발음한 ‘머선 일이고’에서 ‘일이고’를 비슷한 발음의 숫자 129로 표현한 신조어이다.");
        titleList.add("지인지조");
        summaryList.add("지 인생 지가 조진다의 준말.");
        contentList.add("지 인생 지가 조진다는 뜻이다.");
        titleList.add("쉽살재빙");
        summaryList.add("'쉽게만 살아가면 재미없어 빙고!'의 준말.");
        contentList.add("‘쉽게만 살아가면 재미없어 빙고’, 거북이의 노래 ‘빙고’의 가사에서 유래한 것으로, 젊은 사람들이 힘들거나 어려운 일이 생겼을 때 스스로를 위로하기 위해 외치는 말이다.");
        titleList.add("최최차차");
        summaryList.add("'최애는 최애고 차은우는 차은우'의 준말.");
        contentList.add("‘최애는 최애고 차은우는 차은우’ 라는 뜻으로, 최애는 따로 있지만 차은우는 잘생겼다는 뜻이다.");
        titleList.add("스불재");
        summaryList.add("자초한 일을 스스로 한탄함.");
        contentList.add("‘스스로 불러온 재앙’ 이라는 뜻으로, 자신이 자초한 일을 스스로 한탄할 때 쓰인다.");
        titleList.add("707");
        summaryList.add("'개웃겨'를 숫자로 표현한 단어.");
        contentList.add("'개웃겨'의 초성 'ㄱㅇㄱ'를 숫자로 표현한 단어.");
        titleList.add("당모치");
        summaryList.add("'당연히 모든 치킨은 옳다'의 준말.");
        contentList.add("'당연히 모든 치킨은 옳다'의 준말.");
        titleList.add("2000원 비싸짐");
        summaryList.add("팩트 폭력과 같은 뜻.");
        contentList.add("팩트 폭력과 같은 말로, 팩트 폭력 > 뼈 때림 > 뼈 없어짐 > 순살 됨 > 2000원 비싸짐 의 흐름이다.");
        titleList.add("힘숨찐");
        summaryList.add("'힘을 숨긴 찐따'의 준말.");
        contentList.add("‘힘을 숨긴 찐따’ 라는 뜻으로, 겉으로는 안 그래 보이지만 알고 보면 강한 힘을 가졌다는 것을 가르키는 용어이다.");
        titleList.add("완내스");
        summaryList.add("'완전 내 스타일'의 준말.");
        contentList.add("‘완전 내 스타일’ 의 줄임말");
        titleList.add("꾸꾸꾸");
        summaryList.add("'꾸며도 꾸질 꾸질하다'의 준말.");
        contentList.add("'꾸안꾸'의 반댓말로, '꾸며도 꾸질 꾸질하다'의 준말이다.");
        titleList.add("꾸안꾸");
        summaryList.add("'꾸민듯 안 꾸민 듯'의 준말.");
        contentList.add("'꾸꾸꾸'의 반댓말로, '꾸민 듯 안 꾸민 듯한 스타일'의 준말이다.");
        titleList.add("내행부영");
        summaryList.add("타인에게 좋은 일이 생겼을 때 하는 말.");
        contentList.add("‘내가 다 행복하다! 부럽다! 영원하길!’의 줄임말로, 누군가에게 좋은 일이 생겼을때 부러움을 담아 축하해 주는 신조어이다.");
        titleList.add("빠태");
        summaryList.add("'빠른 태세 전환'의 준말.");
        contentList.add("'빠른 태세 전환'의 줄임말이다.");
        titleList.add("저메추");
        summaryList.add("'저녁 메뉴 추천'의 준말.");
        contentList.add("'저녁 메뉴 추천' 의 줄임말이다.");
        titleList.add("무지성");
        summaryList.add("생각 없이 무작정.");
        contentList.add("'생각 없이 무작정'이라는 뜻으로, 애니메이션 '진격의 거인'의 무지성 거인에서 유래한 단어이다.");
        titleList.add("whyrano");
        summaryList.add("왜 이러냐의 경상도 사투리.");
        contentList.add("‘왜 이러냐’의 경상도 사투리 ‘와이라노’를 영어로 적은 표현이다.");
        titleList.add("만반잘부");
        summaryList.add("'만나서 반가워 잘 부탁해'의 준말.");
        contentList.add("'만나서 반가워 잘 부탁해'의 줄임말이다.");
        titleList.add("HDD");
        summaryList.add("'후덜덜'의 신조어.");
        contentList.add("후덜덜, ㅎㄷㄷ를 나타내는 신조어이다.");
        titleList.add("오저치고");
        summaryList.add("'오늘 저녁 치킨 고?'의 준말.");
        contentList.add("'오늘 저녁 치킨 고?'의 줄임말이다.");
        titleList.add("반모");
        summaryList.add("'반말 모드'의 준말.");
        contentList.add("반말 모드의 줄임말로, 인터넷에서 익명으로 대화할 때 반말로 얘기한다는 뜻이다.");
        titleList.add("반박");
        summaryList.add("'반말 모드 박탈'의 준말.");
        contentList.add("반말 모드의 줄임말로, 인터넷에서 익명으로 대화할 때 반말로 얘기한다는 뜻이다.");
        titleList.add("캘박");
        summaryList.add("'캘린더 박제'의 준말.");
        contentList.add("캘린더 박제의 줄임말로, 일정을 캘린더에 저장한다는 것을 뜻한다.");
        titleList.add("주불");
        summaryList.add("'주소 불러'라는 뜻.");
        contentList.add("‘(택배 보낼) 주소 불러’ 라는 뜻이다.");
        titleList.add("악깡버");
        summaryList.add("'악으로 깡으로 버텨라'의 준말.");
        contentList.add("'악으로 깡으로 버텨라'의 줄임말로, 누군가가 뭔가를 잘못하거나 일이 잘 풀릴 때 버티라는 의미로(주로 조롱조로) 사용하는 말이다.");
        titleList.add("혜자");
        summaryList.add("가격 대비 고퀄리티라는 뜻.");
        contentList.add("가격 대비 고퀄리티라는 뜻이며 연예인 김혜자님을 브랜드로 편의점 도시락에서 부터 나온 단어이다.");
        titleList.add("인쇼");
        summaryList.add("'인터넷 쇼핑'의 준말.");
        contentList.add("‘인터넷 쇼핑’ ‘인터넷 쇼핑몰’의 줄임말");
        titleList.add("퍼컬");
        summaryList.add("'퍼스널 컬러'의 준말.");
        contentList.add("‘퍼스널 컬러’ 를 줄인 단어로 개인이 가진 신체의 색과 어울리는 색을 의미한다. 사용자에게 생기가 돌고 활기차 보이도록 연출하는 이미지 관리 따위에 효과적이다.");
        titleList.add("~처돌이");
        summaryList.add("대상의 열렬한 팬이라는 뜻.");
        contentList.add("‘처갓집’ 치킨집 마스코트의 이름에서 유래한 단어로, 주로 '무슨무슨처돌이'의 형태로 쓰이며 앞에 붙은 명사에 쳐돌아버릴 정도로 팬이라는 의미로 쓰인다.");
        titleList.add("유교걸");
        summaryList.add("비교적 보수적으로 보이는 여성을 뜻함.");
        contentList.add("유교와 여자아이(영어로 girl)의 합성어. 지나친 노출이나 문란한 사생활에 거부감을 가지는 전통적인 사고방식을 가진 여성을 의미하는 유행어. 정말로 유교적인 사고방식을 지지한다는 의미로 쓰이기보다는,한국인의 기준에 '너무 막 나간다'는 것에 반감을 느낄 때 '알고 보니 나도 보수적인 여자였네'라고 할 때 주로 쓰이는 표현.");
        titleList.add("톤그로");
        summaryList.add("어색한 화장을 뜻함.");
        contentList.add("색을 뜻하는 영단어 ‘tone’과 문제를 뜻하는 영단어 ‘aggro’를 합쳐 만든 신조어이다. 자신과 어울리지 않는 화장품을 사용해서 어색하다는 의미로 사용된다.");
        titleList.add("헤메코");
        summaryList.add("'헤어, 메이크업, 코디'의 준말.");
        contentList.add("헤어, 메이크업, 코디의 줄임말로 앞글자를 따온 신조어이다.");
        titleList.add("입틀막");
        summaryList.add("너무 놀라 벅차오른다는 뜻.");
        contentList.add("‘입을 틀어막다’를 줄여 이르는 말. 놀라서 벌어진 입을 막을 정도로 벅차오를 때 쓴다.");
        titleList.add("긱잡");
        summaryList.add("단기 일자리를 뜻함.");
        contentList.add("긱(gig)과 직업(job)의 합성어로 긱은 1920년대 미국 재즈클럽이 섭외한 단기 연주자를 부르는데서 유래한 말로, 필요에따라 임시로 계약을 하고 업무를 맡는 단기 일자리를 긱잡이라한다.");
        titleList.add("카공족");
        summaryList.add("카페에서 공부하는 사람들.");
        contentList.add("카페에서 커피나 간식 등을 구매하고 장시간 머무르며 공부하는 사람들을 일컫는 신조어이다.");
        titleList.add("무물");
        summaryList.add("'무엇이든 물어보세요'의 준말.");
        contentList.add("'무엇이든 물어보세요'의 줄임말이다.");
        titleList.add("미공포");
        summaryList.add("'미공개 포토카드'의 준말.");
        contentList.add("‘미공개 포토카드’의 줄임말로 아이돌이 참여하는 행사에서 팬들이 받을 수 있는 포토카드를 의미한다.");
        titleList.add("끌올");
        summaryList.add("'끌어 올린다'의 준말.");
        contentList.add("‘끌어 올린다’의 줄임말로 예전에 올라와 있던 내용을 다시 작성했다는 뜻.");
        titleList.add("달글");
        summaryList.add("특정 주제의 게시글에 달리는 댓글을 뜻함.");
        contentList.add("‘달리는 글’의 줄임말로 특정한 소주제를 하나의 게시글에서 댓글로 달리는 글을 의미한다.");
        titleList.add("많관부");
        summaryList.add("'많은 관심 부탁드립니다'의 준말.");
        contentList.add("'많은 관심 부탁드립니다'의 줄임말이다.");
        titleList.add("ㄱㅇㅇ");
        summaryList.add("'귀여워'의 초성.");
        contentList.add("'귀여워'를 초성만으로 표현한 단어이다.");
        titleList.add("야민정음");
        summaryList.add("단어를 비슷한 모양의 다른 단어로 표기.");
        contentList.add("한글 자모를 모양이 비슷한 것으로 바꾸어 단어를 다르게 표기하는 인터넷 밈이다. 대표적인 예로는 '댕댕이(멍멍이)', '띵작(명작)' '괄도 네넴띤(팔도 비빔면)' 등이있다. 디시인사이드 국내야구 갤러리에서 발전하여 지금과 같은 형태가 되었다.");
        titleList.add("껄무새");
        summaryList.add("'~할껄'을 반복적으로 말하는 사람.");
        contentList.add(" ‘~할껄’과 앵무새를 합성한 신조어로, ~할껄 이라는 말을 반복적으로 하는 앵무새처럼 개인 투자자들이 특정 투자자산을 상승하기 전에 사거나 하락하기 전에 팔지 못하고 후회하는 모습을 반복한다는 점에서 등장한 단어이다.");
        titleList.add("망붕");
        summaryList.add("'망상분자'의 준말.");
        contentList.add("'망상분자'의 줄임말.망상분자의 발음을 뭉갠 망상붕자를 다시 줄여 망붕이된것으로 추정된다. 기본적으로 연예인 등 유명인사에 대한 공상을 사실이라고 믿는 팬을 의미하지만, 자조적으로 쓰일 때는 자신의 공상이 사실이 아닌 것을 알지만 사실이라고 믿고 싶어하거나, 가상인물에 이와 비슷한 망상벽을 보이는 사람까지 포함한다.");
        titleList.add("밀프렙");
        summaryList.add("일주일치 식사를 미리 준비하는 것.");
        contentList.add("1주일치 식사를 한 번에 미리 준비해 놓고 끼니마다 꺼내 먹는 방법으로, 식사(meal)와 준비(preparation)의 합성어이다.");
        titleList.add("뇌절");
        summaryList.add("반복되는 행동으로 상대를 질리게 하는 것.");
        contentList.add("일본의 소년 만화 '나루토'의 등장인물인 하타케 카카시의 기술 뇌절에서 파생된 단어로, 같은 말이나 행동을 반복해 상대를 질리게 하는 것을 부정적으로 지칭하는 신조어다. 유명 리그오브레전드 플레이어 도파의 '1절만 못 하고 2절, 3절 카카시 뇌절까지 한다'에서 유래한 단어이다. 이해할 수 없는 말이나 행동 때문에 뇌의 회로가 끊어지는 것처럼 사고가 정지된다는 뜻으로도 사용되고 있다.");
        titleList.add("트롤");
        summaryList.add("도움이 되지 않고 협조하지 않는 사람.");
        contentList.add("게임할 때 팀에 도움이 되지 않고 협조를 하지 않는 사람을 뜻하거나, 일부러 훼방을 하는 행동 또는사람을 뜻한다.");
        titleList.add("짜짜");
        summaryList.add("'진짜'의 유행어.");
        contentList.add("'진짜'의 유행어. 비슷한 단어로 박박(대박), 나나(존나) 등이 있다.");
        titleList.add("군싹");
        summaryList.add("'군침이 싹 도노'의 준말.");
        contentList.add("'군침이 싹 도노'의 줄임말로, 트위터에서 유래된 유행어이다.");
        titleList.add("구취");
        summaryList.add("'구독 취소'의 준말.");
        contentList.add("유튜브 채널을 구독 취소하는 것을 뜻한다.");
        titleList.add("쌉에이블 / 쌉파서블");
        summaryList.add("무조건 가능하다는 뜻.");
        contentList.add("쌉 +able/ 쌉 +passable 쌉가능. 나는 무조건 할 수 있다. 가능하다. 라는 의미이다.");
        titleList.add("돼지런하다");
        summaryList.add("먹을때 부지런한 사람.");
        contentList.add("먹을때 부지런한 사람을 뜻한다.");
        titleList.add("폰~");
        summaryList.add("작성자가 시인한 조작된 사연 등을 뜻함.");
        contentList.add("커뮤니티 사이트 디시인사이드의 로라 메르시에 마이너 갤러리에서 유래한 단어로, 어느 유저가 '폰씨 성을 가진 사람이 있어. 너무 신기해' 라며 글을 썼는데, 이에 '폰씨 성이 진짜로 있다고..? 중국인 아니야?' 라며 되묻자 '응 봤어. 실은 내가 만들어낸 성씨야 ㅋㅋㅋ 근데 성씨가 폰이라는게 특이한거 같아' 라는 내용에서 파생된 단어이다.");
        titleList.add("검커렁");
        summaryList.add("허언을 하는 사람을 뜻함.");
        contentList.add("검사커플이지렁~의 줄임말로, 커뮤니티 사이트 인스티즈에서 작성된 '둥이들 남친 직업 뭐야?' 게시글에서 유래된 단어이다. 주로 누가 봐도 허언으로 보이는 언행을 하는 사람들을 조롱조로 뜻하는 유행어이다.");
        titleList.add("모청");
        summaryList.add("'모바일 청첩장'의 준말.");
        contentList.add("'모바일 청첩장'의 줄임말이다.");
        titleList.add("h워얼v");
        summaryList.add("'사랑해'를 뒤집은 단어.");
        contentList.add("'사랑해'를 상하로 뒤집고, 좌우로 다시 뒤집은 모양새이다.");
        titleList.add("보라해");
        summaryList.add("'사랑해'의 다른 말.");
        contentList.add("방탄소년단 뷔가 팬들에게 ‘사랑해'라는 말로 쓰며 유행한 단어이다.");
        titleList.add("할매니엘");
        summaryList.add("할머니 스타일을 선호하는 밀레니엄 세대.");
        contentList.add("할머니 + 밀레니엘 ; 할머니들이 입고 먹는 음식을 좋아하는 밀레니엄세대를 지칭한다.");
        titleList.add("믓찌다믓찌다");
        summaryList.add("'멋지다 멋지다'의 유행어.");
        contentList.add("TV 프로그램 스트리트 우먼 파이터에서 댄서 립제이가 응원하며 쓰는 말이다. 억양이 중요하다.");
        titleList.add("작아격리");
        summaryList.add("코로나로 인해 살이 찐 것을 뜻함.");
        contentList.add("코로나로 집콕하며 운동량이 급격히 떨어져 살이 너무 쪄서 옷들이 작아짐을 나타낸다.");
        titleList.add("멍청비용");
        summaryList.add("멍청해서 낭비한 비용을 뜻함.");
        contentList.add("내가 멍청해서 낭비하게 된 비용을 뜻한다.");
        titleList.add("편리미엄");
        summaryList.add("'편리함이 곧 프리미엄'의 준말.");
        contentList.add("편리함+프리미엄. 편리함이 중요 소비 트렌드로, 편리함이 곧 프리미엄이다라는 뜻이다.");
        titleList.add("쫌쫌따리");
        summaryList.add("작은 노력으로 소소한 행복을 누림.");
        contentList.add("조금씩 적고 하찮은 양을 모으는 모습을 뜻하는 단어로, 약간의 노력으로 소소한 행복을 누리고싶어하는 젊은 세대의 트렌드가 반영된 말이다.");
        titleList.add("쩝쩝박사 / 쩝쩝도사");
        summaryList.add("음식에 대해 잘 아는 사람을 뜻함.");
        contentList.add("음식의 맛있는 조합을 잘 아는 사람들을 칭찬할때 쓰는 말이다.");
        titleList.add("랜친실안");
        summaryList.add("'랜에선 친하고 실제론 안 친함'의 준말.");
        contentList.add("랜상에서는 친하지만 실제론 안 친한 사이를 뜻한다.");
        titleList.add("스몸비");
        summaryList.add("스마트폰만 보고 걷는 사람.");
        contentList.add("스마트폰+좀비. 스마트폰만 보고 걷는 사람을 말한다.");
        titleList.add("오놀아놈");
        summaryList.add("'오우 놀 줄 아는 놈인가?'의 준말.");
        contentList.add("'오우 놀 줄 아는 놈인가?'의 줄임말로, 커뮤니티 사이트 디시인사이드의 리그오브레전드 갤러리의 '인싸와 아싸차이 ㅇㄱㄹㅇ ㅂㅂㅂㄱ.txt' 게시글에서 인싸는 뭐든 줄여말한다. 맥도날드에서 점원에게 '상스치콤(상하이 스파이스 치킨버거 콤보) 주세요' 라고 말하면 점원이 '오우~ 놀 줄 아는 놈인가?' 라고 생각한다는 게시글 내용에서 유래되었다.");

        List<Dict> dictList = new ArrayList<>();
        User user = jwtAuthenticateProcessor.getUser(userDetails);
        for (int i = 0; i < titleList.size(); i++) {
            dictList.add(Dict.builder()
                    .firstAuthor(user)
                    .recentModifier(user)
                    .dictName(titleList.get(i))
                    .summary(summaryList.get(i))
                    .content(contentList.get(i))
                    .build());
        }
        dictRepository.saveAll(dictList);
    }
}
