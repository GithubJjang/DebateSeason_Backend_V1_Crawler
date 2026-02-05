package com.debate.croll.producer.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.debate.croll.producer.repository.SseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MonitorService {

	private final SseRepository sseRepository;

	public SseEmitter sendLogs(String userId){
		return sseRepository.save(userId);
	}
}
