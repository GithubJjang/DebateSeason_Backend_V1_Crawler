package com.debate.croll.domain.repository;

import java.util.Optional;

import com.debate.croll.producer.crawler.common.Status;
import com.debate.croll.producer.crawler.mapper.checkpoint.CheckPointProjection;
import com.debate.croll.infrastructure.entity.CheckPointEntity;

public interface CheckPointRepository {

	void save(CheckPointEntity checkPointEntity);

	// 1-1. 체크포인트 존재 유무 확인(Community 전용)
	boolean existsByNameAndCrawlIndex(String name, Integer crawlIndex); // exists는 일단 찾으면 거기서 조회를 멈춘다. 이와 달리 count는 다 센다.

	// 1-2. 체크포인트 존재 유무 확인(News 전용)
	boolean existsByNameAndSubKeyAndCrawlIndex(String name, Integer subKey, Integer crawlIndex);

	// 2. 최신 체크포인트 불러오기
	Optional<CheckPointProjection> findLatestCheckPoint();

	// 2. 체크 포인트 업데이트 (날짜만 업데이트를 하는데 굳이 객체를 조회해서 변경 감지를 할 필요가 없다.)
	void updateCheckPoint( // (Community 전용)
		String updatedAt,
		String name,
		int crawlIndex
	);

	void updateCheckPoint( // (News 전용)
		String updatedAt,
		String name,
		Integer subKey,
		int crawlIndex
	);


	// 3. 상태값이 DONE이 체크포인트 불러오기, 날짜 전용 업데이트 메소드.
	boolean existsByStatus(Status status);

	void updateLastCheckPointOnly(
		String updatedAt,
		String status)
		;

	// 4. 오늘 날짜 성공한 checkpoint 개수 불러오기.
	Long countTodaySuccessCheckPoint();
}
