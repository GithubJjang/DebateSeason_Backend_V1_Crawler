package com.debate.croll.producer.service;

import org.springframework.stereotype.Service;

import com.debate.croll.producer.entity.Error;
import com.debate.croll.producer.repository.ErrorRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ErrorService {

	private final ErrorRepository errorRepository;

	public void save(Error error){
		errorRepository.save(error);
	}
}
