package com.debate.croll.infrastructure.repository.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.debate.croll.domain.ErrorRepository;
import com.debate.croll.infrastructure.repository.jpa.ErrorJpaRepository;
import com.debate.croll.producer.entity.ErrorEntity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ErrorRepositoryImpl implements ErrorRepository {

	private final ErrorJpaRepository errorJpaRepository;

	@Override
	public List<ErrorEntity> findTodayErrors() {
		return errorJpaRepository.findTodayErrors();
	}

	@Override
	public Long countTodayErrors() {
		return errorJpaRepository.countTodayErrors();
	}
}
