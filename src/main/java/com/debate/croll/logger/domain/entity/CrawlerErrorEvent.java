package com.debate.croll.logger.domain.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import com.debate.croll.logger.common.crawler.OriginClass;
import com.debate.croll.logger.common.crawler.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class CrawlerErrorEvent {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;


	@Enumerated(EnumType.STRING) // 서비스. ( ex. CRAWLER )
	private OriginClass OriginClass;

	@Enumerated(EnumType.STRING) // 타입. ( ex. News or Community )
	private Type type;

	@Column(name = "name")
	private String name;

	@Column(name = "tag1")
	private Integer tag1;

	@Column(name = "tag2")
	private Integer tag2;

	@Column(name = "exception_class")
	private String exceptionClass; // 예외 클래스.

	@Column(name = "message") // 에러 메시지.
	private String message;

	@Column(name = "stacktrace")
	private String stackTrace;

	@CreatedDate
	@Column(name = "created_at", updatable = false) // 생성일.
	private LocalDateTime createdAt;

}
