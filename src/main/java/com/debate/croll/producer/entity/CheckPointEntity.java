package com.debate.croll.producer.entity;

import org.springframework.data.annotation.CreatedDate;

import com.debate.croll.producer.crawler.common.Status;
import com.debate.croll.producer.crawler.common.Type;

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

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "check_point")
public class CheckPointEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "name") // 크롤링 대상 이름
	private String name;

	@Column(name = "sub_key")
	private Integer subKey;

	@Column(name = "crawl_index") // 실행 인덱스, index는 예약어라서 안됨.
	private Integer crawlIndex;

	@Enumerated(EnumType.STRING)// COMMUNITY, NEWS -> 만약 ENUM 값에 변경이 발생할 경우, 새로 테이블을 생성해야 한다.
	private Type type;

	@CreatedDate
	@Column(name = "updated_at", columnDefinition = "TEXT")// updatable = true이 기본값
	private String updated_at; // 생성일. SQLite는 날짜 타입이 없어서 -> TEXT로 저장

	@Enumerated(EnumType.STRING)// STEADY, REBOOT -> 만약 ENUM 값에 변경이 발생할 경우, 새로 테이블을 생성해야 한다.
	private Status status;

}
