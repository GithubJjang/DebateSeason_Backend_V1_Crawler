package com.debate.croll.producer.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {

	private Long id;
	private String title;
	private String url;
	private String src;
	private String category;
	private String media;
	private String type;// news, community, youtube
	private int count;// 조회수
	private int checked;
	private String createdAt;

}
