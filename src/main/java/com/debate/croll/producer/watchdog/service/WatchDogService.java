package com.debate.croll.producer.watchdog.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.debate.croll.infrastructure.entity.MediaEntity;
import com.debate.croll.infrastructure.repository.jpa.MediaJpaRepository;
import com.debate.croll.producer.watchdog.CrawlerProgressDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class WatchDogService {


	private final MediaJpaRepository mediaJpaRepository;

	public Optional<CrawlerProgressDto> checkStatus() { // 상태 감지

		// 만약 없으면 null인 Optional<CrawlerProgressDto>을 반환한다.
		return mediaJpaRepository.findLatestMedia()
			.map(checkPointEntity -> {
				CrawlerProgressDto crawlerProgressDto = new CrawlerProgressDto();

				crawlerProgressDto.setId(checkPointEntity.getId());// Id
				crawlerProgressDto.setUpdatedAt(checkPointEntity.getUpdated_at());// 초기화 날짜
				crawlerProgressDto.setStatus(checkPointEntity.getStatus());// 현재 상태(DONE, REBOOT, ...)

				return crawlerProgressDto;
			});
	}
}
