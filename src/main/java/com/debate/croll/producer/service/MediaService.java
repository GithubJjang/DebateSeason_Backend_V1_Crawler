package com.debate.croll.producer.service;


import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.debate.croll.producer.entity.Media;
import com.debate.croll.producer.response.MediaResponse;
import com.debate.croll.producer.repository.MediaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class MediaService {

	private final MediaRepository mediaRepository;

	// 1. Media 저장
	public void save(Media media){

		mediaRepository.save(media);

	}

	// 2. LastId 이후 limit만큼 가져오기.
	public List<MediaResponse> findMediaAfterId(long lastId, int limit){


		// lastId 이후 5건 가져오기. ex) 5 -> 6,7,8,9,10 가져오기
		List<Media> mediaList = mediaRepository.findMediaAfterId(lastId, limit);

		if(mediaList!=null){

			// DTO로 만들기
			List<MediaResponse> mediaResponseList = new ArrayList<>();

			System.out.println("Send to extractor");
			for(Media m : mediaList){
				mediaResponseList.add(m.toModel());
				System.out.print(m.getId()+" ");
			}
			System.out.println();

			return mediaResponseList;

		}
		else{
			log.info("there isn't new raw media");
			return null;
		}

	}


	// 2.
	public List<MediaResponse> findMediaAfterLimitOrderByIdAsc(int limit) {

		//Unchecked(0)로 필터링 + PK 오름차순(오래된 순으로) + 5개 가져오기
		List<Media> mediaList = mediaRepository.findMediaAfterLimitOrderByIdAsc(limit);


		System.out.println("Send to extractor");
		for(Media m : mediaList){
			System.out.print(m.getId()+" ");
		}
		System.out.println();
		
		
		// 데이터가 있으면 반환, 없으면 빈 배열 반환
		List<MediaResponse> mediaResponseList = new ArrayList<>();

		for(Media m : mediaList){
			mediaResponseList.add(m.toModel());
		}

		return mediaResponseList;
	}
}
