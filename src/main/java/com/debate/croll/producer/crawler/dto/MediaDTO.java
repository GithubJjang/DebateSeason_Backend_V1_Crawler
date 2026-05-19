package com.debate.croll.producer.crawler.dto;

public record MediaDTO(
	String title,
	String url,
	String src,
	String category,
	String media,
	String type,
	Integer count,
	String createdAt
) {}



