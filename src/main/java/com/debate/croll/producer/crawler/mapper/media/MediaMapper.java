package com.debate.croll.producer.crawler.mapper.media;

import com.debate.croll.producer.crawler.dto.MediaDTO;
import com.debate.croll.producer.entity.MediaEntity;

public class MediaMapper {

	// 1. DTO -> 엔티티로 전환
	public static MediaEntity toEntity(MediaDTO.CreateMediaDTO dto) {

		return MediaEntity.builder()
			.title(dto.title())
			.url(dto.url())
			.src(dto.src())
			.category(dto.category())
			.media(dto.media())
			.type(dto.type())
			.count(dto.count())
			.createdAt(dto.createdAt())
			.build();
	}
}
