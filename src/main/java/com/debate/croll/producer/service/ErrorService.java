package com.debate.croll.producer.service;

import org.springframework.stereotype.Service;

import com.debate.croll.producer.crawler.mapper.error.ErrorMapper;
import com.debate.croll.producer.crawler.dto.ErrorDTO;
import com.debate.croll.producer.entity.ErrorEntity;
import com.debate.croll.producer.repository.ErrorJpaRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ErrorService {

	private final ErrorJpaRepository errorJpaRepository;
	public void save(ErrorDTO.CreateErrorDTO errorDTO){

		ErrorEntity errorEntity = ErrorMapper.toEntity(errorDTO);

		errorJpaRepository.save(errorEntity);
	}
}
