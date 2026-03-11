package com.debate.croll.producer.crawler.source.community.config;

import lombok.Getter;

@Getter
public enum CommunityNameList {
	BobaeDream("보배드림"),
	Clien("클리앙"),
	FmKorea("에펨코리아"),
	HumorUniv("웃긴대학"),
	MlbPark("엠엘비파크"),
	Ppompu("뽐뿌"),
	RuliWeb("루리웹"),
	TodayHumor("오늘의유머"),
	News("뉴스");

	private final String name;

	CommunityNameList(String name) {
		this.name = name;
	}

}
