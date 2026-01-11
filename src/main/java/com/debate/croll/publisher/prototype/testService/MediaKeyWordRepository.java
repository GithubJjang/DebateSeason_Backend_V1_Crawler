package com.debate.croll.publisher.prototype.testService;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.debate.croll.publisher.domain.entity.MediaKeyWord;

@Repository
public interface MediaKeyWordRepository extends JpaRepository<MediaKeyWord,Long> {
}
