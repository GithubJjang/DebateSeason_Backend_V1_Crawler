package com.debate.croll.producer.crawler.request;

public class MediaDTO {

	// 동작/도메인 명사/Request of Response
	public record CreateMediaDTO( // @Getter,@Setter,@Builder,@AllArgs...,@NoArgs... => record 하나로 가능
		String title,
		String url,
		String src,
		String category,
		String media,
		String type,
		Integer count,
		String createdAt
	){}

}



