package com.teamproj.backend.service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import com.teamproj.backend.Repository.dict.DictYoutubeUrlRepository;
import com.teamproj.backend.model.dict.Dict;
import com.teamproj.backend.model.dict.DictYoutubeUrl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class YoutubeService {
    private final DictYoutubeUrlRepository dictYoutubeUrlRepository;

    // API key
    @Value("${youtube.apikey}")
    private String apiKey;

    /**
     * Global instance
     * HttpTransport : instance of the HTTP transport.
     * JsonFactory : instance of the JSON factory.
     * NUMBER_OF_VIDEOS_RETURNED : instance of the max number of videos we want returned (50 = upper limit per page).
     * youtube : instance of Youtube object to make all API requests.
     */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final long NUMBER_OF_VIDEOS_RETURNED = 50;
    private static YouTube youtube;

    /*
     * Youtube Data Api v3 구조 설명
     * -----필수요소-----
     * part : id(영상의 id값) 또는 snippet(영상의 상세정보) 중 무엇을 받아올지 결정(YoutubeSearch.youtubeSearch 메소드 안에 설정해두었습니다.)
     * q : 요청할 쿼리문입니다. 해당 구문을 기준으로 검색 결과가 출력됩니다. this.youtubeSearch()의 파라미터인 String query 가 해당 기능을 수행하고 있습니다.
     *
     * ----수령 데이터 제한----
     * q : 필수요소로, 검색에 사용할 쿼리를 지정합니다.
     * maxResults : 받아올 데이터의 수량을 지정합니다. 데이터의 수령 할당량과 밀접한 연관이 있습니다.
     *              YoutubeSearch.youtubeSearch() 내의 NUMBER_OF_VIDEOS_RETURNED 상수로 지정되어 있습니다.
     *
     * channelId : 지정된 채널Id의 값들만 받아오도록 합니다.
     * order : 쿼리 검색 결과를 어떤 기준으로 시행할지 결정합니다. 기본값은 연관성이 높은 영상으로 설정됩니다.
     *      date : 최근 영상부터
     *      rating : 높은 평가순
     *      relevance : 기본값. 관련 영상으로 호출
     *      title : 제목 문자순(오름차순)
     *      videoCount : 크리에이터가 업로드한 동영상 수를 기준으로
     *      viewCount : 조회수순(내림차순)
     * pageToken : 검색 결과를 받아올 페이지를 설정합니다.
     * publishedAfter : 지정 시간 이후에 업로드된 영상만 검색합니다. 값은 RFC 3339 형식이 지정된 날짜-시간 값(1970-01-01T00:00:00Z)입니다.
     * publishedBefore : 지정 시간 이전에 업로드된 영상만 검색합니다. 값은 RFC 3339 형식이 지정된 날짜-시간 값(1970-01-01T00:00:00Z)입니다.
     * type : 검색 결과의 타입을 지정합니다.
     *      channel : 채널만 찾습니다.
     *      playlist : 재생목록만 찾습니다.
     *      video : 단일 동영상만 찾습니다.
     * ** Q) playlist와 video는 왜 구분하나요? A) 유튜브 플레이어는 재생목록 전용과 단일영상 재생용의 플레이어가 다릅니다. 한 플레이어에서 불러와지지 않기 때문에 이렇게 해야합니다!
     *
     * 프로젝트에서 사용하는 목록들만 작성했습니다.
     *
     * ------결과값------
     * kind : 검색된 영상의 타입입니다. 형식은 youtube#searchListResponse 입니다.
     * nextPageToken : 다음 페이지를 확인할 수 있게 해주는 토큰입니다.
     * prevPageToken : 이전 페이지를 확인할 수 있게 해주는 토큰입니다.
     * pageInfo : 현재 페이지 정보를 나타냅니다.
     * pageInfo.totalResults : 검색 결과의 총 개수를 나타냅니다.
     * pageInfo.resultsPerPage : API 사용의 결과 개수를 나타냅니다.
     * items[] : 검색 기준과 일치하는 결과 목록을 가져옵니다.
     *
     * ------items[] 내용물------
     *
     */
    public List<DictYoutubeUrl> getYoutubeSearchResult(Dict dict, String query) {
        /*
            결과값 받아오기 프로세스
            1. 쿼리의 공백 제거
            2. 결과 타이틀의 공백 제거
            3. 결과 타이틀을 한 글자씩 돌면서 쿼리의 String.length() 만큼의 subString 시행
            4. 3.의 결과물이 쿼리와 얼마나 유사한지 탐색
            5. 67% 이상(2글자일 경우 전체일치, 3글자일 경우 2글자 이상 일치)일 경우 get.
            6. 최대 3개까지 저장하여 반환.
            7. 이론상 좋아.
         */
        // 검색어가 2글자보다 작을 경우 신뢰도 있는 결과를 얻기 어려우므로 생략.
        if (query.replaceAll(" ", "").length() < 2) {
            return new ArrayList<>();
        }
        List<SearchResult> searchResultList = youtubeSearch(query);

        List<DictYoutubeUrl> dictYoutubeUrlList = new ArrayList<>();
        for (SearchResult searchResult : searchResultList) {
            ResourceId rId = searchResult.getId();

            // Double checks the kind is video.
            if (rId.getKind().equals("youtube#video")) {
                String title = searchResult.getSnippet().getTitle();
                String channel = searchResult.getSnippet().getChannelTitle();
                Thumbnail thumbnail = (Thumbnail) searchResult.getSnippet().getThumbnails().get("medium");
                String thumbNail = thumbnail.getUrl();
                String youtubeUrl = rId.getVideoId();

                // 영상의 제목과 검색어가 유사한지 비교한다. 신뢰도 높은 결과를 위해.
                if (isSimilar(title, query)) {
                    dictYoutubeUrlList.add(DictYoutubeUrl.builder()
                            .dict(dict)
                            .title(title)
                            .channel(channel)
                            .thumbNail(thumbNail)
                            .youtubeUrl(youtubeUrl)
                            .build());
                }
            }

            if (dictYoutubeUrlList.size() >= 3) {
                break;
            }
        }

        return dictYoutubeUrlList;
    }

    private boolean isSimilar(String title, String query) {
        // 1. query, title 의 공백 제거
        query = query.replaceAll(" ", "");
        title = title.replaceAll(" ", "");

        // 2. query, title의 텍스트 길이 측정
        int queryLength = query.length();
        int titleLength = title.length();

        // 3. query 가 title 보다 길 경우 검색 대상에서 제외. 검색 결과의 신뢰도가 낮을 가능성이 높기 때문에 배제함.
        if (queryLength > titleLength) {
            return false;
        }

        // 4. titleLength.subString(i, queryLength) 과 비교해가며 얼마나 유사한지 비교.
        for (int i = 0; i < titleLength - queryLength; i++) {
            String titleSubString = title.substring(i, queryLength + i);
            // 5. 유사도가 0.67을 초과할 경우 즉시 true 반환.
            double similarity = similarity(titleSubString, query);
            if (similarity >= 0.67) {
                return true;
            }
        }
        // 6. 유사도가 0.67을 초과하지 못 할 경우 비슷하지 않음으로 판정, false 반환.
        return false;
    }

    // 문장의 유사도를 확인하는 메소드. 레벤슈타인 거리 알고리즘 채택.
    private double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;

        if (s1.length() < s2.length()) {
            longer = s2;
            shorter = s1;
        }

        int longerLength = longer.length();
        if (longerLength == 0) return 1.0;
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    private int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();
        int[] costs = new int[s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];

                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        }

                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }

            if (i > 0) costs[s2.length()] = lastValue;
        }

        return costs[s2.length()];
    }

    /**
     * Initializes YouTube object to search for videos on YouTube (Youtube.Search.List). The program
     * then prints the names and thumbnails of each of the videos (only first 50 videos).
     *
     * @param query request query to Youtube Data API v3. origin parameter name : q
     *              <p>
     *              Handling Exceptions : IOException, GoogleJsonResponseException
     */
    public List<SearchResult> youtubeSearch(String query) {
        try {
            /*
             * The YouTube object is used to make all API requests. The last argument is required, but
             * because we don't need anything initialized when the HttpRequest is initialized, we override
             * the interface and provide a no-op function.
             */
            youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, request -> {
            })
                    .setApplicationName("youtube-cmdline-search-sample").build();

            // Get query term from user.
            String queryTerm = getInputQuery(query);

            YouTube.Search.List search = youtube.search().list("id,snippet");
            /*
             * It is important to set your API key from the Google Developer Console for
             * non-authenticated requests (found under the Credentials tab at this link:
             * console.developers.google.com/). This is good practice and increased your quota.
             */
            search.setKey(apiKey);
            search.setQ(queryTerm);
            /*
             * We are only searching for videos (not playlists or channels). If we were searching for
             * more, we would add them as a string like this: "video,playlist,channel".
             */
            search.setType("video");
            /*
             * This method reduces the info returned to only the fields we need and makes calls more
             * efficient.
             */
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/channelTitle,snippet/thumbnails/medium/url)");
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
            SearchListResponse searchResponse = search.execute();

            Optional<List<SearchResult>> searchResultList = Optional.of(searchResponse.getItems());

            return searchResultList.orElseGet(ArrayList::new);
//            searchResultList.ifPresent(searchResults -> prettyPrint(searchResults.iterator(), queryTerm));
        } catch (GoogleJsonResponseException e) {
            System.err.println("Google Service Error : " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }

        // other unknown error : return null
        return null;
    }

    /*
     * Returns a query term (String) from user via the terminal.
     */
    private static String getInputQuery(String query) {
        System.out.println("Input search query is : " + query);

        if (query.length() < 1) {
            // If nothing is entered, defaults to "YouTube Developers Live."
            // @instead : throw IllegalArgumentException
            throw new IllegalArgumentException("값을 입력하세요.");
        }
        return query;
    }

    /**
     * Prints out all SearchResults in the Iterator. Each printed line includes title, id, and
     * thumbnail.
     *
     * @param iteratorSearchResults Iterator of SearchResults to print
     * @param query                 Search query (String)
     */
    private static void prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query) {

        System.out.println("\n=============================================================");
        System.out.println(
                "   First " + NUMBER_OF_VIDEOS_RETURNED + " videos for search on \"" + query + "\".");
        System.out.println("=============================================================\n");

        if (!iteratorSearchResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }

        while (iteratorSearchResults.hasNext()) {

            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();

            // Double checks the kind is video.
            if (rId.getKind().equals("youtube#video")) {
                Thumbnail thumbnail = (Thumbnail) singleVideo.getSnippet().getThumbnails().get("default");

                System.out.println(" Video Id" + rId.getVideoId());
                System.out.println(" Title: " + singleVideo.getSnippet().getTitle());
                System.out.println(" Thumbnail: " + thumbnail.getUrl());
                System.out.println("\n-------------------------------------------------------------\n");
            }
        }
    }
}
