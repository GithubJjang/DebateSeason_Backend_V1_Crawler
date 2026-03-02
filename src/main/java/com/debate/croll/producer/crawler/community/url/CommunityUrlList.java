package com.debate.croll.producer.crawler.community.url;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

import com.debate.croll.producer.crawler.community.list.Community;

import io.sentry.Sentry;
import jakarta.annotation.PostConstruct;

@Component
public class CommunityUrlList { // 싱글톤

	@PostConstruct
	public void initUrlList(){
		urlMap.put(Community.BobaeDream.name(),"https://www.bobaedream.co.kr/list?code=best"); // 보배드림
		urlMap.put(Community.Clien.name(), "https://www.clien.net/service/group/clien_all?&od=T33"); // 클리앙
		urlMap.put(Community.FmKorea.name(), "https://www.fmkorea.com/index.php?mid=politics&sort_index=pop&order_type=desc"); // 에펨코리아
		urlMap.put(Community.HumorUniv.name(), "https://m.humoruniv.com/board/list.html?table=pds&st=better"); // 웃대
		urlMap.put(Community.MlbPark.name(), "https://mlbpark.donga.com/mp/best.php?b=bullpen&m=like"); // 엠팍
		urlMap.put(Community.Ppompu.name(), "https://www.ppomppu.co.kr/hot.php?category=2"); // 뽐뿌
		urlMap.put(Community.RuliWeb.name(), "https://bbs.ruliweb.com/best/political"); // 루리웹
		urlMap.put(Community.TodayHumor.name(), "https://www.todayhumor.co.kr/board/list.php?table=bestofbest"); // 오늘의 유머
	}

	// 1. CommunityUrl을 저장하는 리스트.
	public static Map<String,String> urlMap = new HashMap<>();

	// 2. url 가져오기
	public static String getUrl(String name) throws NullPointerException{

		String url = urlMap.get(name);

		if(url != null){

			return url;

		}
		else{

			NullPointerException exception = new NullPointerException("CommunityUrlList.getUrl - Error : 존재하지 않은 커뮤니티 조회 "+ name);

			Sentry.captureException(
				exception
			);

			throw exception;

		}

	}

}
