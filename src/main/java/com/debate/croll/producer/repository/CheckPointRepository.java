package com.debate.croll.producer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.debate.croll.producer.crawler.common.Status;
import com.debate.croll.producer.crawler.mapper.checkpoint.CheckPointProjection;
import com.debate.croll.producer.entity.CheckPointEntity;

@Repository
public interface CheckPointRepository extends JpaRepository<CheckPointEntity,Long> {

	// 1-1. 체크포인트 존재 유무 확인(Community 전용)
	boolean existsByNameAndCrawlIndex(String name, Integer crawlIndex); // exists는 일단 찾으면 거기서 조회를 멈춘다. 이와 달리 count는 다 센다.
	
	// 1-2. 체크포인트 존재 유무 확인(News 전용)
	boolean existsByNameAndSubKeyAndCrawlIndex(String name, Integer subKey, Integer crawlIndex);

	// 2. 최신 체크포인트 불러오기
	@Query(value = """
        SELECT name, sub_key, crawl_index, type, status
        FROM check_point
        ORDER BY updated_at DESC
        LIMIT 1
        """, nativeQuery = true)
	Optional<CheckPointProjection> findLatestCheckPoint();

	// 2. 체크 포인트 업데이트 (날짜만 업데이트를 하는데 굳이 객체를 조회해서 더티체킹???)
	@Modifying
	@Query(
		value = """
        UPDATE check_point
        SET updated_at = :updatedAt
        WHERE name = :name
        AND sub_key = :subkey
        AND crawl_index = :crawlIndex
        """,
		nativeQuery = true
	)
	void updateCheckPoint(@Param("updatedAt") String updatedAt,
		@Param("name") String name,
		@Param("subkey") Integer subKey,
		@Param("crawlIndex") int crawlIndex);

	// 3. 상태값이 DONE이 체크포인트 불러오기, 날짜 전용 업데이트 메소드.
	boolean existsByStatus(Status status);

	@Modifying
	@Query(
		value = """
		UPDATE check_point
		SET updated_at = :updatedAt
		WHERE status = :status
		""",
		nativeQuery = true
	)
	void updateLastCheckPointOnly(@Param("updatedAt") String updatedAt,
		@Param("status") String status);
}
