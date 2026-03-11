package com.debate.croll.producer.crawler.source.news.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@Component
public class NewsUrlList {

	public final LinkedHashMap<String,String> newsList = new LinkedHashMap<>(); // 순서를 보장해야만 한다.

	public final List<String> newsNameList = new ArrayList<>(); // 언론사 이름

	public final List<Integer> category = Arrays.asList(100,101,102,104); // category 번호

	@PostConstruct
	public void init() { // 의존성 주입 -> 초기화 때 실행

		newsList.put("한국경제","https://media.naver.com/press/015");
		newsList.put("매일경제","https://media.naver.com/press/009");
		newsList.put("한계례","https://media.naver.com/press/028");
		newsList.put("조선일보","https://media.naver.com/press/023");
		newsList.put("전자신문","https://media.naver.com/press/030");
		newsList.put("중앙일보","https://media.naver.com/press/025");
		newsList.put("연합뉴스","https://media.naver.com/press/422");
		newsList.put("YTN","https://media.naver.com/press/052");
		newsList.put("MBC","https://media.naver.com/press/214");
		newsList.put("SBS","https://media.naver.com/press/055");
		newsList.put("KBS","https://media.naver.com/press/056");

		//keys = newsList.keySet(); 순서가 뒤틀려서, 중복으로 출력이 된다...

		newsNameList.add("한국경제");
		newsNameList.add("매일경제");
		newsNameList.add("한계례");
		newsNameList.add("조선일보");
		newsNameList.add("전자신문");
		newsNameList.add("중앙일보");
		newsNameList.add("연합뉴스");
		newsNameList.add("YTN");
		newsNameList.add("MBC");
		newsNameList.add("SBS");
		newsNameList.add("KBS");
	}
}
