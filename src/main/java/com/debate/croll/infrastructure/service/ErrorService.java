package com.debate.croll.infrastructure.service;

import org.springframework.stereotype.Service;

import com.debate.croll.producer.crawler.mapper.error.ErrorMapper;
import com.debate.croll.producer.crawler.dto.ErrorDTO;
import com.debate.croll.producer.entity.ErrorEntity;
import com.debate.croll.infrastructure.repository.jpa.ErrorJpaRepository;

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
