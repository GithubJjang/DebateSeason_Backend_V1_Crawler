package com.debate.croll.producer.crawler.service;

import org.springframework.stereotype.Service;

import com.debate.croll.producer.crawler.mapper.error.ErrorMapper;
import com.debate.croll.producer.crawler.request.ErrorDTO;
import com.debate.croll.producer.entity.ErrorEntity;
import com.debate.croll.producer.repository.ErrorRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ErrorService {

	private final ErrorRepository errorRepository;
	public void save(ErrorDTO.CreateErrorDTO errorDTO){

		ErrorEntity errorEntity = ErrorMapper.toEntity(errorDTO);

		errorRepository.save(errorEntity);
	}
}
