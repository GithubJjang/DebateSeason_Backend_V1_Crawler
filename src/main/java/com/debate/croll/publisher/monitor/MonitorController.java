package com.debate.croll.publisher.monitor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MonitorController {

	private final MonitorService monitorService;

	@GetMapping("/home")
	public String home(){

		return "hello SpringBoot";
	}

	@GetMapping("/monitor")
	public SseEmitter sendLogs(@RequestParam("userId")String userId){

		return monitorService.sendLogs(userId);
	}
}
