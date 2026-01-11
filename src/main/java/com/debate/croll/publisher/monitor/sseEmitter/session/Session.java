package com.debate.croll.publisher.monitor.sseEmitter.session;
import java.util.List;
import java.util.Map;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.debate.croll.logger.domain.entity.CrawlerErrorEvent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Session {

	private SseEmitter sseEmitter;
	//private Map<String,String> logMap;
	//private List<CrawlerErrorEvent> errorEventList;

	private int progressLogOffset; // 진행 로그 전용 오프셋
	private int errorLogOffset;


}
