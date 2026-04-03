package com.debate.croll.producer.service;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.debate.croll.producer.crawler.mapper.checkpoint.CheckPointMapper;
import com.debate.croll.producer.crawler.mapper.media.MediaMapper;
import com.debate.croll.producer.crawler.dto.CheckPointDTO;
import com.debate.croll.producer.crawler.dto.MediaDTO;
import com.debate.croll.producer.crawler.common.Type;
import com.debate.croll.producer.entity.CheckPointEntity;
import com.debate.croll.producer.entity.MediaEntity;
import com.debate.croll.producer.repository.CheckPointJpaRepository;
import com.debate.croll.producer.repository.MediaJpaRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CrawlerApplicationService {

	private final MediaJpaRepository mediaJpaRepository;
	private final CheckPointJpaRepository checkPointJpaRepository;

	@Transactional
	public void saveMediaAndCheckPoint(MediaDTO.CreateMediaDTO mediaDTO, CheckPointDTO.CreateCheckPointDTO checkPointDTO){

		// 1.
		MediaEntity mediaEntity = MediaMapper.toEntity(mediaDTO);
		mediaJpaRepository.save(mediaEntity);

		// 2.
		String name = checkPointDTO.name();
		Integer subKey = checkPointDTO.subKey();
		Integer index  = checkPointDTO.crawlIndex();
		Type type = Type.valueOf(mediaDTO.type());

		switch (type){
			case COMMUNITY -> {
				if(!checkPointJpaRepository.existsByNameAndCrawlIndex(name, index)){ // 로그가 없다면 <- 엔티티 조회가 아니므로, 새로 만든다.
					CheckPointEntity checkPointEntity = CheckPointMapper.toEntity(checkPointDTO);
					checkPointJpaRepository.save(checkPointEntity);
				}
				else{ // 로그가 있다면, updated_at만 수정을 한다. -> 영속성 컨텍스트 등록없이 바로 DB에 등록
					String updatedAt = checkPointDTO.updatedAt();
					checkPointJpaRepository.updateCheckPoint(updatedAt,name,index);
				}
			}
			case NEWS -> {
				if(!checkPointJpaRepository.existsByNameAndSubKeyAndCrawlIndex(name, subKey, index)){ // 로그가 없다면 <- 엔티티 조회가 아니므로, 영속성 컨텍스트 등록이 안됨
					CheckPointEntity checkPointEntity = CheckPointMapper.toEntity(checkPointDTO);
					checkPointJpaRepository.save(checkPointEntity);
				}
				else{
					String updatedAt = checkPointDTO.updatedAt();
					checkPointJpaRepository.updateCheckPoint(updatedAt,name,subKey,index);
				}
			}
			default ->
				throw new NoSuchElementException("There is no "+type.name());
		}
	}
}
