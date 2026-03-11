package com.debate.croll.producer.crawler.mapper.checkpoint;

import com.debate.croll.producer.crawler.request.CheckPointDTO;
import com.debate.croll.producer.crawler.request.MediaDTO;
import com.debate.croll.producer.entity.CheckPointEntity;
import com.debate.croll.producer.entity.MediaEntity;

public class CheckPointMapper {

	// 1. DB에 CheckPoint를 저장하기 위한 Entity
	public static CheckPointEntity toEntity(CheckPointDTO.CreateCheckPointDTO dto){

		return CheckPointEntity.builder()
			.name(dto.name())
			.subKey(dto.subKey())
			.crawlIndex(dto.crawlIndex())
			.type(dto.type())
			.updated_at(dto.updatedAt())
			.status(dto.status())
			.build();
	}

}
