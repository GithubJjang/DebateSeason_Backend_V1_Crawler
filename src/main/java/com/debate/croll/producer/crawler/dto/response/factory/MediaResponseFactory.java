package com.debate.croll.producer.crawler.dto.response.factory;

import com.debate.croll.producer.crawler.dto.response.MediaRawResponse;
import com.debate.croll.producer.entity.MediaEntity;

public class MediaResponseFactory{ // 매번 빌더로 만들기 번거롭다.

	public MediaRawResponse createMediaRawResponse(MediaEntity mediaEntity){
			return MediaRawResponse.builder()
				.id(mediaEntity.getId())
				.title(mediaEntity.getTitle())
				.url(mediaEntity.getUrl())
				.src(mediaEntity.getSrc())
				.category(mediaEntity.getCategory())
				.media(mediaEntity.getMedia())
				.type(mediaEntity.getType())
				.count(mediaEntity.getCount())
				.createdAt(mediaEntity.getCreatedAt())
				.build();
	}

}
