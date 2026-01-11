package com.debate.croll.logger.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.debate.croll.logger.domain.entity.CrawlerErrorEvent;

public interface CrawlerErrorEventRepository extends JpaRepository<CrawlerErrorEvent,Long> {

	@Query(
		value = """
		SELECT *
		FROM CrawlerErrorEvent
		WHERE created_at >= CURRENT_DATE
		  AND created_at < DATEADD('DAY', 1, CURRENT_DATE);
    """,
		nativeQuery = true
	)
	List<CrawlerErrorEvent> findTodayErrors(); // 재사용성을 고려하면 이게 훨씬 낫다.

	@Query(
		value = """
		SELECT count(*)
		FROM CrawlerErrorEvent
		WHERE created_at >= CURRENT_DATE
		  AND created_at < DATEADD('DAY', 1, CURRENT_DATE);
    """,
		nativeQuery = true
	)
	Long countTodayErrors(); // 재사용성을 고려하면 이게 훨씬 낫다.

}
