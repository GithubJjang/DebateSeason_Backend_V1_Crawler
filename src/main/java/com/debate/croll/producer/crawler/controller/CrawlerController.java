package com.debate.croll.producer.crawler.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.debate.croll.producer.common.ApiResponse;
import com.debate.croll.producer.crawler.dto.response.MediaRawResponse;
import com.debate.croll.producer.service.MediaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
public class CrawlerController {

	private final MediaService mediaService;

	private final int limit = 10; // 3건 가져오기

	@GetMapping("/")
	public String connectionCheck(){
		return "connect to crawler";
	}

	@GetMapping("/media")
	public ApiResponse<List<MediaRawResponse>> getCrawlerRecords(
		@RequestParam(name = "lastId", required = false) Long lastId
	){
		if(lastId!=null){
			return mediaService.findMediaAfterId(lastId,limit);

		}
		else{ // 최초 요청을 보낼 경우
			return mediaService.findMediaAfterLimitOrderByIdAsc(limit);
		}
	}
	
}
