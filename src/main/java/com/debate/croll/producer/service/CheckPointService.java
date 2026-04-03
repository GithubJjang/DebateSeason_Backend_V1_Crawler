package com.debate.croll.producer.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.debate.croll.producer.crawler.common.Status;
import com.debate.croll.producer.crawler.mapper.checkpoint.CheckPointProjection;
import com.debate.croll.producer.entity.CheckPointEntity;
import com.debate.croll.producer.repository.CheckPointJpaRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CheckPointService {

	private final CheckPointJpaRepository checkPointJpaRepository;

	public CheckPointProjection getMostRecentCheckPoint(){

		// checkPoint가 있으면, CheckPointProjection을 반환하고 아니면 null을 반환한다.
		return checkPointJpaRepository.findLatestCheckPoint().orElse(null);
	}

	@Transactional
	public void updateLastCheckPoint() { // 마지막 체크포인트를 기록한다.

		if (!checkPointJpaRepository.existsByStatus(Status.DONE)) { //만약 없다면, 새로 체크포인트를 기록한다.

			// 없으면 새로 체크포인트를 생성한다
			CheckPointEntity lastCheckPointEntity = CheckPointEntity.builder()
				.name(null)
				.subKey(null)
				.crawlIndex(null)
				.type(null)
				.updated_at(LocalDateTime.now().toString())
				.status(Status.DONE)
				.build();

			checkPointJpaRepository.save(lastCheckPointEntity);
		} else {

			// 이미 있다면, 날짜면 갱신을 한다.
			checkPointJpaRepository.updateLastCheckPointOnly(
				LocalDateTime.now().toString(), // 날짜 갱신
				Status.DONE.getName() // DONE만 업데이트
			);

		}

	}

}
