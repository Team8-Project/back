# 밈글밈글(MemegleMemegle) - BackEnd

<img src="https://user-images.githubusercontent.com/70641418/151412036-345d6b9d-2657-459d-920a-def5be916f1c.jpg">

</br>

## 🧧 서비스 소개
인터넷에서 유명하는 합성 소스 또는 유행을 의미하는 밈과  
새롭게 생겨나는 신조어를 사전 형식으로 검색 할 수 있는 사이트입니다.
</br>

<a href="https://memegle.xyz/">프로젝트  사이트로 이동</a>

<a href="https://enormous-duck-a5d.notion.site/8-5510f20898534129bd1b728b92d92870">프로젝트 노션으로 이동</a>

</br>

## 📆 프로젝트 기간
2021.12.18. (토) ~ 2021.01.28. (금)

</br>

## 👥 팀원

- Back-End    

<code><a href="https://github.com/Zabee52">김용빈(팀장)</a></code>  
  
<code><a href="https://github.com/yarogono">임전혁</a></code>
  
</br>
    
- Front-End

<code><a href="https://github.com/undriedspring">이한샘</a></code>  
  
<code><a href="https://github.com/zubetcha">정주혜</a></code>
  
<code><a href="https://github.com/zhiyeonyi">이지연</a></code>

</br>

## 📚 스택

<img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white">  <img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=Spring Boot&logoColor=white">  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=MySQL&logoColor=white">  <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=Redis&logoColor=white"> </br>
<img src="https://img.shields.io/badge/Spring Data JPA-6DB33F?style=for-the-badge">
<img src="https://img.shields.io/badge/QueryDSL-3874D8?style=for-the-badge">
<img src="https://img.shields.io/badge/Youtube Data API v3-FF0000?style=for-the-badge&logo=YouTube&logoColor=white">


## 🛠 아키텍처
  
<img src="https://user-images.githubusercontent.com/70641418/151467454-da82b310-6249-4480-9204-8a4ace733ba6.JPG">
 
</br>
</br>

## 📋 DB설계

<img src="https://user-images.githubusercontent.com/70641418/151432549-bf519850-4146-471f-8cee-5e51bb932c88.png">

</br>  
  
## ✒Trouble Shooting / Challenge

<details markdown="1">
<summary>알림 기능) 새로운 알림이 생겼을 경우에만 DB에서 받아오기 </summary>  
<br>
우리 페이지는 알람을 받는데 웹소켓을 사용하지 않았다. 페이지를 이동할 때마다 알람 정보를 요청하는 식으로 작동한 것이다.  
이것은 변화량이 적은 알람 기능에 있어서 비효율적인 동작이다. 그렇기 때문에 이 부분에 대한 개선이 필요했다.  
지금부터 적을 부분은 당장 적용한 부분(백엔드 단일)과 이후에 더욱 강화하여 적용할 수 있는 부분(프론트엔드와 협업)들이다.  

### **1. 지금은 너무 비효율적이다**
    
![https://blog.kakaocdn.net/dn/boS0Tz/btrr5TN9HNC/0xQzXV28jawVRHkK24HAZ1/img.png](https://blog.kakaocdn.net/dn/boS0Tz/btrr5TN9HNC/0xQzXV28jawVRHkK24HAZ1/img.png)

현재의 방식은 페이지를 이동할 때마다 백엔드에 알람 정보를 요청, 사용자의 알람 테이블에서 알람 정보를 모두 가져오는 식으로 되어있다. 이는 자주 바뀌지 않는 내용인데도 주기적으로 DB에 요청하기 때문에 DB는 불필요한 부하를 안게 되고, 이것은 상당히 마음이 불편한 일이다. 요청이 만약에 많이 밀린다면 속도 저하 요인이 될 수 있는 부분이다. 이 부분에 대한 개선이 필요했다.

그렇게 고민을 하던 중, 우리 페이지의 특성을 떠올렸다.

우리 페이지는 알람 기능을 웹소켓을 이용해 구현하지 않았기 때문에, 페이지 이동시마다 요청을 한다. **어, 그러면 페이지 이동을 할 때마다 데이터가 바뀌었는지를 체크해서 DB를 방문할지 여부를 결정하면 되는 것 아닌가?**

### **2. Redis를 써보자.**

![https://blog.kakaocdn.net/dn/lmHut/btrr9e5l0S0/y7MPbgS3tMsoFgNgoqRSfK/img.png](https://blog.kakaocdn.net/dn/lmHut/btrr9e5l0S0/y7MPbgS3tMsoFgNgoqRSfK/img.png)

사용자에게 AlarmCheck 컬럼을 추가하고, 이 값을 이용해 바뀌지 않았을 땐 redis에서 값을 받아오다가, 바뀌었을 경우엔 DB에서 값을 가져오고, MySQL은 redis에 새로운 값을 세팅해주는 것이다. 그리고 이후엔 다시 redis를 이용해 통신한다. 이 방식을 적용함으로써 DB에 방문하는 빈도를 크게 낮출 수 있었고, 이것은 측정하진 못 했지만 사이트 자체의 퍼포먼스 개선에 영향을 주었을 것이라고 생각한다.

### **3. 조금만 더 개선해보자**

여기까지는 백엔드에서 단독으로 적용 가능한 부분이었다. 이 방식은 백엔드에서 단독으로 적용할 수 있기 때문에 프론테은드의 노력이 추가되지 않는다는 장점이 있지만, 알람을 읽었을 경우 읽은 알람에 대한 처리를 별도로 해줘야 하는 점 때문에 다소 불필요한 절차가 추가될 가능성이 있다. 이 문제를 해결하기 위해선 프론트엔드와의 협업이 필요하다고 생각했고, 이 과정을 통해 추가적으로 작업에 수행되는 스텝 수를 줄일 수 있을 것이라는 생각이 들었다.

이하는 실제 시스템에 적용되진 않았으며, 이론상 생각만 해 본 부분이라 도표가 없는 점 양해 부탁.

1. 기본적으로 알람 정보는 받아오고나면 로컬 스토리지에서 관리.

2. 페이지 이동시마다 리액트가 스프링부트에 알람 정보를 요청.

3. 스프링부트는 redis에서 값을 확인. 이 값은 변화 여부만 기록함.

4. 변화되지 않았을 경우 리액트는 로컬 스토리지의 데이터 반환.

이렇게 만들 경우, 사용자 데이터에 알람 체크 컬럼이 필요가 없어진다. 백엔드 측에서의 관리 포인트가 하나 줄어드는 것이다. 그리고 알람 확인 여부를 프론트에서 관리할 수 있기에 redis의 값을 수정하는 절차도 줄일 수 있게 된다.
	
</details>

<details markdown="1">
<summary>사전 기능) 사전의 동시 수정 제한</summary>
<br>
우리가 만들었던 밈글밈글 사이트는 오픈사전 형식이었기 때문에 최초 작성자가 아니라고 해도 누구나 편집을 할 수 있게 되어 있다. 이 말은, 다른 사람들이 하나의 용어사전 글을 동시에 수정할 수 있다는 것이다. 이는 수정을 일찍 마친 사람의 데이터가 손쉽게 소실될 수 있음을 의미하기 때문에 이런 부분들을 제한할 수 있어야 했다.

당시 나왔던 대안은 두 가지가 있었다.

1. 동시에 수정이 불가능하도록 하자!

2. 용어 사전 페이지의 편집 기능을 다른 뜻 추가 기능처럼 만들어서 아래에 붙이도록 하자!

개인적으로 2.의 적용안이 좋다고 생각했지만, 이미 페이지 개발은 2.와 다른 형식으로 완료된 상태였기 때문에, 이를 적용하기 위해선 추가적인 프론트엔드의 작업 수요가 발생하는 상황이었다. 안그래도 시안에 쫓겨 작업을 하던 프론트엔드 분들에게 부하를 가할 수는 없는 노릇이었고, 이에 따라 자연스럽게 1.로 적용하게 되었다.

처음에는 이 방식을 어떻게 적용할까에 대해 여기저기 찾아다녔다. 처음 찾아본 곳은 선배 기수의 처리 방식이었다.

마침 동시처리를 제한하는 형식의 사이트가 이미 있었기 때문에 그곳의 소스코드를 봤다. 항해99 2기의 판단(Pandan)이라는 팀의 작업물이었는데, 이곳은 DB만 사용하는 방식이었기 때문에 나는 조금 색다르게 적용해보고 싶었다.

그래서 Redis를 쓰기로 했다.

그럼 이제 본문으로 넘어간다.

### **1. 기존엔 동시 수정이 너무나도 자유로웠다.**

![슬라이드26](https://user-images.githubusercontent.com/93498724/151788196-418997ff-2bc0-431a-9a10-4086f444c000.PNG)

이에 따라 늦게 작성한 사용자의 데이터만 반영이 되고, 먼저 작성한 사람의 데이터는 수정내역의 뒤안길로 사라져버리는 문제가 있었다. 사용자가 정성스럽게 작성한 데이터가 소실되는 것은 매우 뼈아픈 일이었기 때문에, 이 문제를 해결하기 위해서는 둘 중 한명만 수정이 가능하게 해야했다. 이를 위해 우리는 만료기한을 지정해준 상태로 레디스에 값을 세팅 해주는 방식을 적용했다.

### **2. 나쁘지 않은 성과**

```java
// DictServicepublic Boolean getDictHealthCheck(Long dictId, UserDetailsImpl userDetails) {
        String key = DICT_HEALTH_CHECK_KEY + ":" + dictId;
        String result = redisService.getDictHealth(key);
        String username = userDetails.getUsername();

// 키가 없으면 자신의 아이디로 등록, 키가 있을 경우 내 아이디와 일치하면 갱신if(result == null || username.equals(result)){
            redisService.setDictHealth(key, username);
            return true;
        }

// 키가 이미 존재하면서 나의 키가 아니면 수정 불가.return false;
    }
```

```java
// RedisServicepublic String getDictHealth(String key) {
        return redisStringTemplate.opsForValue().get(key);
    }

    public void setDictHealth(String key, String str) {
        redisStringTemplate.opsForValue().set(key, str);
        redisStringTemplate.expire(key, 15, TimeUnit.SECONDS);
    }
```

여기에 추가로 프론트엔드에서 레디스 만료시간 갱신을 위한 요청을 주기적으로 보내주면 끝나는 코드였다. 폴링 방식이지만 10초 정도에 한 번씩의 요청은 나쁘지 않은 것 같은데? 싶다.

레디스를 사용하니 추가적인 이점도 얻을 수 있었다. 바로 코드의 간소화였다. DB를 탐색하며 값이 변동하는지 아닌지에 대한 컬럼을 확인해서 처리를 하고 사용자가 비정상적인 경로로 나가는 것을 체크해야 하고... 그러한 방식을 간단한 방식으로 처리할 수 있게 되었다. 위의 코드가 전부다.


![슬라이드27](https://user-images.githubusercontent.com/93498724/151788214-5da4eeb4-25ad-4719-b28c-32a31a614dda.PNG)
![슬라이드28](https://user-images.githubusercontent.com/93498724/151788224-a4a8b3f4-2136-4f20-abc1-92789d44fdb6.PNG)


도표로 보자면 이러한 방식이다.

### **3. 존재하는 잠재적 위험**

그럼에도 잠재적인 위험은 존재하는데, 바로 **사용자가 만료 시간(15초) 이상 인터넷 연결이 끊겨있는 상태가 발생할 경우 작성한 내용의 기록 권한을 잃어버리는 것**이다. 현재는 프론트엔드 측에서 임시 저장을 해주는 등의 기능을 제공한다면 될 것이라고 생각하고 있다. 하지만 더욱 뾰족한 해결방안이 필요해보인다. 아직 이 부분에 대해선 답을 찾지 못 했다.
</details>

[기타 트러블슈팅](https://www.notion.so/BE-669d3c8366a1406b85d538a33966a9ea)  
[기타 성능 향상 전략](https://www.notion.so/BE-4e9f7960af9a436784d21f55382564e2)  
[성능 향상 전략 및 챌린지 요약본(pdf)](https://drive.google.com/file/d/130hJu6DtMyVALjyZb-hkZrDIAg2RfhLF/view?usp=sharing)
