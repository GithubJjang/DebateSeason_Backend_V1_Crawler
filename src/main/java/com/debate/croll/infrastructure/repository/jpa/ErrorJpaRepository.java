package com.debate.croll.infrastructure.repository.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.debate.croll.infrastructure.entity.ErrorEntity;

@Repository
public interface ErrorJpaRepository extends JpaRepository<ErrorEntity,Long> {

	// 1. 오늘 에러 목록들 불러오기.
	@Query(
		value = """
    SELECT *
    FROM Error
    WHERE created_at >= datetime('now', 'start of day')
      AND created_at < datetime('now', 'start of day', '+1 day')
    ORDER BY id ASC
    """,
		nativeQuery = true
	)
	List<ErrorEntity> findTodayErrors(); // 재사용성을 고려하면 이게 훨씬 낫다.

	// 2. 오늘 에러 발생 건수 카운트 하기
	@Query(
		value = """
    SELECT count(*)
    FROM Error
    WHERE created_at >= date('now')
      AND created_at < date('now', '+1 day')
    """,
		nativeQuery = true
	)
	Long countTodayErrors(); // 재사용성을 고려하면 이게 훨씬 낫다.

}

