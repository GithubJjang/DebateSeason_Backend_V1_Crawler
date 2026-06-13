package com.debate.croll.infrastructure.service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.debate.croll.domain.repository.CheckPointRepository;
import com.debate.croll.producer.crawler.mapper.checkpoint.CheckPointMapper;
import com.debate.croll.producer.crawler.mapper.media.MediaMapper;
import com.debate.croll.producer.crawler.dto.CheckPointDTO;
import com.debate.croll.producer.crawler.dto.MediaDTO;
import com.debate.croll.producer.crawler.common.Type;
import com.debate.croll.infrastructure.entity.CheckPointEntity;
import com.debate.croll.infrastructure.entity.MediaEntity;
import com.debate.croll.infrastructure.repository.jpa.MediaJpaRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CrawlerApplicationService {

	private final MediaJpaRepository mediaJpaRepository;
	private final CheckPointRepository checkPointRepository;

	@Transactional
	public void saveMediaAndCheckPoint(MediaDTO mediaDTO, CheckPointDTO checkPointDTO){

		// 1.
		MediaEntity mediaEntity = MediaMapper.toEntity(mediaDTO);
		mediaJpaRepository.save(mediaEntity);

		// 2.
		String name = checkPointDTO.name();
		Integer subKey = checkPointDTO.subKey();
		Integer crawlIndex  = checkPointDTO.crawlIndex();
		Type type = Type.valueOf(mediaDTO.type());

		switch (type){
			case COMMUNITY -> {
				if(!checkPointRepository.existsByName(name)){ // 로그가 없다면 <- 엔티티 조회가 아니므로, 새로 만든다.
					CheckPointEntity checkPointEntity = CheckPointMapper.toEntity(checkPointDTO);
					checkPointRepository.save(checkPointEntity);
				}
				else{ // 로그가 있다면, crawlIndex와 날짜를 갱신한다.
					String updatedAt = checkPointDTO.updatedAt();
					checkPointRepository.updateCheckPoint(updatedAt,name,crawlIndex);
				}
			}
			case NEWS -> {
				if(!checkPointRepository.existsByNameAndSubKeyAndCrawlIndex(name, subKey, crawlIndex)){ // 로그가 없다면 <- 엔티티 조회가 아니므로, 영속성 컨텍스트 등록이 안됨
					CheckPointEntity checkPointEntity = CheckPointMapper.toEntity(checkPointDTO);
					checkPointRepository.save(checkPointEntity);
				}
				else{
					String updatedAt = checkPointDTO.updatedAt();
					checkPointRepository.updateCheckPoint(updatedAt,name,subKey,crawlIndex);
				}
			}
			default ->
				throw new NoSuchElementException("There is no "+type.name());
		}
	}
}
