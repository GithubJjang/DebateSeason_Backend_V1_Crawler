package com.debate.croll.producer.crawler.service;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.debate.croll.producer.crawler.mapper.checkpoint.CheckPointMapper;
import com.debate.croll.producer.crawler.mapper.media.MediaMapper;
import com.debate.croll.producer.crawler.request.CheckPointDTO;
import com.debate.croll.producer.crawler.request.MediaDTO;
import com.debate.croll.producer.crawler.type.Type;
import com.debate.croll.producer.entity.CheckPointEntity;
import com.debate.croll.producer.entity.MediaEntity;
import com.debate.croll.producer.repository.CheckPointRepository;
import com.debate.croll.producer.repository.MediaRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CrawlerService {

	private final MediaRepository mediaRepository;
	private final CheckPointRepository checkPointRepository;

	@Transactional
	public void saveMediaAndCheckPoint(MediaDTO.CreateMediaDTO mediaDTO, CheckPointDTO.CreateCheckPointDTO checkPointDTO){

		// 1.
		MediaEntity mediaEntity = MediaMapper.toEntity(mediaDTO);
		mediaRepository.save(mediaEntity);

		// 2.
		String name = checkPointDTO.name();
		Integer subKey = checkPointDTO.subKey();
		Integer index  = checkPointDTO.crawlIndex();
		Type type = Type.valueOf(mediaDTO.type());

		switch (type){

			case COMMUNITY -> {

				if(checkPointRepository.existsByNameAndCrawlIndex(name,index)==false){ // 로그가 없다면 <- 엔티티 조회가 아니므로, 영속성 컨텍스트 등록이 안됨

					CheckPointEntity checkPointEntity = CheckPointMapper.toEntity(checkPointDTO);
					checkPointRepository.save(checkPointEntity);
				}
				else{ // 로그가 있다면, updated_at만 수정을 한다. -> 영속성 컨텍스트 등록없이 바로 DB에 등록

					String updatedAt = checkPointDTO.updatedAt();
					log.info("successfully updated!");
					checkPointRepository.updateCheckPoint(updatedAt,name,subKey,index);
				}

			}
			case NEWS -> {

				System.out.println("subKey is "+ subKey);

				if(checkPointRepository.existsByNameAndSubKeyAndCrawlIndex(name,subKey,index)==false){ // 로그가 없다면 <- 엔티티 조회가 아니므로, 영속성 컨텍스트 등록이 안됨

					CheckPointEntity checkPointEntity = CheckPointMapper.toEntity(checkPointDTO);
					checkPointRepository.save(checkPointEntity);
				}
				else{ // 로그가 있다면, updated_at만 수정을 한다. -> 영속성 컨텍스트 등록없이 바로 DB에 등록

					String updatedAt = checkPointDTO.updatedAt();
					log.info("successfully updated!");
					checkPointRepository.updateCheckPoint(updatedAt,name,subKey,index);
				}

			}

			default ->
				throw new NoSuchElementException("There is no "+type.name());

		}



	}

}
