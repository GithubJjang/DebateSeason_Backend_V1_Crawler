package com.debate.croll.infrastructure.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.debate.croll.domain.repository.ErrorRepository;

import com.debate.croll.producer.crawler.mapper.error.ErrorMapper;
import com.debate.croll.producer.crawler.dto.error.CrawlerErrorDTO;
import com.debate.croll.infrastructure.entity.ErrorEntity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ErrorService {

	private final ErrorRepository errorRepository;
	public void save(CrawlerErrorDTO crawlerErrorDTO){
		ErrorEntity errorEntity = ErrorMapper.toEntity(crawlerErrorDTO);
		errorRepository.save(errorEntity);
	}

}
