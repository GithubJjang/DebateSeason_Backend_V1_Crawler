package com.debate.croll.producer.entity;

import org.springframework.data.annotation.CreatedDate;

import com.debate.croll.producer.response.MediaResponse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	//@Column(name = "title",columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
	@Column(name = "title")
	private String title;

	@Column(name = "url")
	private String url;

	@Column(name = "src")
	private String src;

	@Column(name = "category")
	private String category;

	@Column(name = "media")
	private String media;

	@Column(name = "type")
	private String type;// news, community, youtube

	@Column(name = "count")
	private Integer count;// 조회수

	@CreatedDate
	@Column(name = "created_at", updatable = false, columnDefinition = "TEXT")
	private String createdAt; // 생성일. SQLite는 날짜 타입이 없어서 -> TEXT로 저장

	public MediaResponse toModel(){

		return MediaResponse.builder()
			.id(id)
			.title(title)
			.url(url)
			.src(src)
			.category(category)
			.media(media)
			.type(type)
			.count(count)
			.createdAt(createdAt)
			.build();

	}

}
