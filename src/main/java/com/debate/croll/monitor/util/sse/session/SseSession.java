package com.debate.croll.monitor.util.sse.session;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SseSession {

	private SseEmitter sseEmitter;
	//private int successLogOffset; // 진행 로그 전용 오프셋
	private int errorLogOffset;


}
