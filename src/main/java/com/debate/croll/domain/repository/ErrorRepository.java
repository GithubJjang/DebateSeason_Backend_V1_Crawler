package com.debate.croll.domain.repository;

import java.util.List;

import com.debate.croll.infrastructure.entity.ErrorEntity;

public interface ErrorRepository {

	// 1. 오늘 에러 목록들 불러오기.
	List<ErrorEntity> findTodayErrors(); // 재사용성을 고려하면 이게 훨씬 낫다.

	// 2. 오늘 에러 발생 건수 카운트 하기
	Long countTodayErrors(); // 재사용성을 고려하면 이게 훨씬 낫다.
}
