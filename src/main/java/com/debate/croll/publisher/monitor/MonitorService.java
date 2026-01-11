package com.debate.croll.publisher.monitor;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MonitorService {

	private final SseRepository sseRepository;

	public SseEmitter sendLogs(String userId){
		return sseRepository.save(userId);
	}
}
