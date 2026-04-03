package com.debate.croll.domain;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.debate.croll.producer.entity.MediaEntity;

public interface MediaRepository {

	void save(MediaEntity mediaEntity);

	// 1. PK 오름차순(오래된 순으로) + 10개 가져오기
	List<MediaEntity> findMediaAfterLimitOrderByIdAsc(
		int limit
	);

	// 2. PK 오름차순 + lastId 이후 limit 건 가져오기. 이전 1,2,3,4,5 -> 5 이후의 것 가져오기
	List<MediaEntity> findMediaAfterId(
		long lastId,
		int limit
	);
}
