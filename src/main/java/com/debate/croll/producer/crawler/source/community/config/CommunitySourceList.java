package com.debate.croll.producer.crawler.source.community.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.debate.croll.producer.crawler.source.community.item.BobaeDream;
import com.debate.croll.producer.crawler.source.community.item.Clien;
import com.debate.croll.producer.crawler.source.community.item.FmKorea;
import com.debate.croll.producer.crawler.source.community.item.HumorUniv;
import com.debate.croll.producer.crawler.source.community.item.MlbPark;
import com.debate.croll.producer.crawler.source.community.item.Ppompu;
import com.debate.croll.producer.crawler.source.community.item.RuliWeb;
import com.debate.croll.producer.crawler.source.community.item.TodayHumor;
import com.debate.croll.producer.crawler.source.community.template.AbstractCommunitySource;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class CommunitySourceList {

	private final BobaeDream bobaeDream;
	private final Clien clien;
	private final FmKorea fmKorea;
	private final HumorUniv humorUniv;
	private final MlbPark mlbPark;
	private final Ppompu ppompu;
	private final RuliWeb ruliWeb;
	private final TodayHumor todayHumor;

	private final List<AbstractCommunitySource> sourceList = new ArrayList<>();

	@PostConstruct
	public void init(){
		sourceList.add(bobaeDream);
		sourceList.add(clien);
		sourceList.add(fmKorea);
		sourceList.add(humorUniv);
		sourceList.add(mlbPark);
		sourceList.add(ppompu);
		sourceList.add(ruliWeb);
		sourceList.add(todayHumor);

	}

	// @Getter를 쓰면, 위 필드변수 다 반환을 해야함. -> 캡슐화 안됨.
	public List<AbstractCommunitySource> getSourceList(){
		return this.sourceList;
	}


}
