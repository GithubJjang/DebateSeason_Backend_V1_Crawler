package com.debate.croll.producer.service;


import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.debate.croll.producer.common.ApiResponse;
import com.debate.croll.producer.crawler.dto.response.MediaRawResponse;
import com.debate.croll.producer.common.Status;
import com.debate.croll.producer.crawler.dto.response.factory.MediaResponseFactory;
import com.debate.croll.producer.entity.MediaEntity;
import com.debate.croll.producer.repository.MediaJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class MediaService {

	private final MediaJpaRepository mediaJpaRepository;

	// 1. Media 저장
	public void save(MediaEntity mediaEntity){

		mediaJpaRepository.save(mediaEntity);

	}

	// 2. LastId 이후 limit만큼 가져오기.
	public ApiResponse<List<MediaRawResponse>> findMediaAfterId(long lastId, int limit){

		MediaResponseFactory factory = new MediaResponseFactory();

		// lastId 이후 5건 가져오기. ex) 5 -> 6,7,8,9,10 가져오기
		List<MediaEntity> mediaEntityList = mediaJpaRepository.findMediaAfterId(lastId, limit);

		if(mediaEntityList!=null){

			// DTO로 만들기
			List<MediaRawResponse> mediaRawResponseList = new ArrayList<>();
			for(MediaEntity m : mediaEntityList){

				MediaRawResponse rawResponse = factory.createMediaRawResponse(m);
				mediaRawResponseList.add(rawResponse);

			}

			return (ApiResponse<List<MediaRawResponse>>)new ApiResponse(
				Status.SUCCESS,
				Status.SUCCESS.getCode(),
				"크롤러 원시 데이터 전송 성공",
				mediaRawResponseList);
		}
		else{
			log.info("there isn't new raw media");
			return null;
		}

	}


	// 2.
	public ApiResponse<List<MediaRawResponse>> findMediaAfterLimitOrderByIdAsc(int limit) {

		MediaResponseFactory factory = new MediaResponseFactory();

		//Unchecked(0)로 필터링 + PK 오름차순(오래된 순으로) + 5개 가져오기
		List<MediaEntity> mediaEntityList = mediaJpaRepository.findMediaAfterLimitOrderByIdAsc(limit);

		// 데이터가 있으면 반환, 없으면 빈 배열 반환
		List<MediaRawResponse> mediaRawResponseList = new ArrayList<>();
		for(MediaEntity m : mediaEntityList){

			MediaRawResponse rawResponse = factory.createMediaRawResponse(m);
			mediaRawResponseList.add(rawResponse);
		}

		return (ApiResponse<List<MediaRawResponse>>)new ApiResponse(
			Status.SUCCESS,
			Status.SUCCESS.getCode(),
			"크롤러 원시 데이터 전송 성공",
			mediaRawResponseList);
	}
}
