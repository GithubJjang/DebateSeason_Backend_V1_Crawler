package com.debate.croll.monitor.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.debate.croll.monitor.util.SseSessionStore;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MonitorService {

	private final SseSessionStore sseSessionStore;

	public SseEmitter sendLogs(String userId){
		return sseSessionStore.save(userId);
	}
}
