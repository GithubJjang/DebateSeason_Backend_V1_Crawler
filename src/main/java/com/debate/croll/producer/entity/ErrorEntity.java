package com.debate.croll.producer.entity;

import org.springframework.data.annotation.CreatedDate;

import com.debate.croll.producer.crawler.common.OriginClass;
import com.debate.croll.producer.crawler.common.Type;
import com.debate.croll.monitor.response.error.ErrorFormatMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "error")
public class ErrorEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Enumerated(EnumType.STRING) // 서비스. ( ex. CRAWLER )
	private OriginClass OriginClass;

	@Enumerated(EnumType.STRING) // 타입. ( ex. News or Community )
	private Type type;

	@Column(name = "name")
	private String name;

	@Column(name = "exception_class")
	private String exceptionClass; // 예외 클래스.

	@Column(name = "message") // 에러 메시지.
	private String message;

	@Column(name = "url")
	private String url; // 주소

	@CreatedDate
	@Column(name = "created_at", updatable = false, columnDefinition = "TEXT")
	private String createdAt; // 생성일. SQLite는 날짜 타입이 없어서 -> TEXT로 저장

	public ErrorFormatMapper toErrorFormatMapper(){

		return ErrorFormatMapper.builder()
			.originClass(OriginClass)
			.type(type)
			.exceptionClass(exceptionClass)
			.message(message)
			.name(name)
			.createdAt(createdAt)
			.build();

	}
}
