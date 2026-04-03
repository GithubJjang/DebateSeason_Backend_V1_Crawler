package com.debate.croll.infrastructure.repository.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.debate.croll.producer.entity.MediaEntity;

@Repository
public interface MediaJpaRepository extends JpaRepository<MediaEntity,Long> {

	// 1. PK 오름차순(오래된 순으로) + 10개 가져오기
	@Query(
		value = """
            SELECT *
            FROM media
            ORDER BY id ASC
            LIMIT :limit
        """,
		nativeQuery = true
	)
	List<MediaEntity> findMediaAfterLimitOrderByIdAsc(
		@Param("limit") int limit
	);

	// 2. PK 오름차순 + lastId 이후 limit 건 가져오기. 이전 1,2,3,4,5 -> 5 이후의 것 가져오기
	@Query(
		value = """
        SELECT *
        FROM media
        WHERE id > :lastId
        ORDER BY id ASC
        LIMIT :limit
    """,
		nativeQuery = true
	)
	List<MediaEntity> findMediaAfterId(
		@Param("lastId") long lastId,
		@Param("limit") int limit
	);

}
