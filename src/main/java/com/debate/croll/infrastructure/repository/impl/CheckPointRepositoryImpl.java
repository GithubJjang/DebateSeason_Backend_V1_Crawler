package com.debate.croll.infrastructure.repository.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.debate.croll.domain.CheckPointRepository;
import com.debate.croll.infrastructure.repository.jpa.CheckPointJpaRepository;
import com.debate.croll.producer.crawler.common.Status;
import com.debate.croll.producer.crawler.mapper.checkpoint.CheckPointProjection;
import com.debate.croll.producer.entity.CheckPointEntity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class CheckPointRepositoryImpl implements CheckPointRepository {

	private final CheckPointJpaRepository checkPointJpaRepository;

	@Override
	public void save(CheckPointEntity checkPointEntity) {
		checkPointJpaRepository.save(checkPointEntity);
	}

	@Override
	public boolean existsByNameAndCrawlIndex(String name, Integer crawlIndex) {
		return checkPointJpaRepository.existsByNameAndCrawlIndex(name, crawlIndex);
	}

	@Override
	public boolean existsByNameAndSubKeyAndCrawlIndex(String name, Integer subKey, Integer crawlIndex) {
		return checkPointJpaRepository.existsByNameAndSubKeyAndCrawlIndex(name,subKey,crawlIndex);
	}

	@Override
	public Optional<CheckPointProjection> findLatestCheckPoint() {
		return checkPointJpaRepository.findLatestCheckPoint();
	}

	@Override
	public void updateCheckPoint(String updatedAt, String name, int crawlIndex) {
		checkPointJpaRepository.updateCheckPoint(updatedAt,name,crawlIndex);
	}

	@Override
	public void updateCheckPoint(String updatedAt, String name, Integer subKey, int crawlIndex) {
		checkPointJpaRepository.updateCheckPoint(updatedAt,name,subKey,crawlIndex);
	}

	@Override
	public boolean existsByStatus(Status status) {
		return checkPointJpaRepository.existsByStatus(status);
	}

	@Override
	public void updateLastCheckPointOnly(String updatedAt, String status) {
		checkPointJpaRepository.updateLastCheckPointOnly(updatedAt,status);
	}

	@Override
	public Long countTodaySuccessCheckPoint() {
		return checkPointJpaRepository.countTodaySuccessCheckPoint();
	}
}
