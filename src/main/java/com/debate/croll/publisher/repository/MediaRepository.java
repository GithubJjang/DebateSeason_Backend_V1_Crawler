package com.debate.croll.publisher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.debate.croll.publisher.domain.entity.Media;
@Repository
public interface MediaRepository extends JpaRepository<Media,Long> {

}
