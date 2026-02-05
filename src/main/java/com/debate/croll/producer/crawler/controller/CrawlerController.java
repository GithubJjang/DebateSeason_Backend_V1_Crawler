package com.debate.croll.producer.crawler.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.debate.croll.producer.crawler.request.IdListRequest;
import com.debate.croll.producer.response.MediaResponse;
import com.debate.croll.producer.service.MediaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
public class CrawlerController {

	private final MediaService mediaService;

	private final int limit = 3; // 3건 가져오기

	@GetMapping("/")
	public String connectionCheck(){
		return "connect to crawler";
	}

	@PostMapping("/crawler/records")
	public List<MediaResponse> getCrawlerRecords(
		@RequestBody IdListRequest idListRequest
	){
		// 1. 전송해야 할 데이터 전송하기
		List<Long> idList = idListRequest.getIdList();

		if(!idList.isEmpty()){ // idList 값이 있는 상황 -> extractor 서버를 최초 실행할 경우

			// 최댓값
			long lastId = idList.get(0);

			// lastId 이후 5건 가져오기. ex) 5 -> 6,7,8,9,10 가져오기
			return mediaService.findMediaAfterId(lastId,limit);

		}
		else{ // idList 값이 없는 상황 -> 최초로 extractor가 실행이 된 경우.
			return mediaService.findMediaAfterLimitOrderByIdAsc(limit);
		}
	}
	
}
