package com.debate.croll.infrastructure.repository.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.debate.croll.domain.MediaRepository;
import com.debate.croll.infrastructure.repository.jpa.MediaJpaRepository;
import com.debate.croll.producer.entity.MediaEntity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class MediaRepositoryImpl implements MediaRepository {

	private final MediaJpaRepository mediaJpaRepository;

	@Override
	public void save(MediaEntity mediaEntity) {
		mediaJpaRepository.save(mediaEntity);
	}

	@Override
	public List<MediaEntity> findMediaAfterLimitOrderByIdAsc(int limit) {
		return mediaJpaRepository.findMediaAfterLimitOrderByIdAsc(limit);
	}

	@Override
	public List<MediaEntity> findMediaAfterId(long lastId, int limit) {
		return mediaJpaRepository.findMediaAfterId(lastId,limit);
	}
}
