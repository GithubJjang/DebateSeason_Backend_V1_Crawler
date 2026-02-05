package com.debate.croll.producer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.debate.croll.producer.entity.Error;

public interface ErrorRepository extends JpaRepository<Error,Long> {


	// @Query(
	// 	value = """
	// 	SELECT *
	// 	FROM CrawlerErrorEvent
	// 	WHERE created_at >= CURRENT_DATE
	// 	  AND created_at < DATEADD('DAY', 1, CURRENT_DATE);
    // """,
	// 	nativeQuery = true
	// )

	@Query(
		value = """
    SELECT *
    FROM Error
    WHERE created_at >= datetime('now', 'start of day')
      AND created_at < datetime('now', 'start of day', '+1 day')
    """,
		nativeQuery = true
	)
	List<Error> findTodayErrors(); // 재사용성을 고려하면 이게 훨씬 낫다.

	// @Query(
	// 	value = """
	// 	SELECT count(*)
	// 	FROM CrawlerErrorEvent
	// 	WHERE created_at >= CURRENT_DATE
	// 	  AND created_at < DATEADD('DAY', 1, CURRENT_DATE);
    // """,
	// 	nativeQuery = true
	// )

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
