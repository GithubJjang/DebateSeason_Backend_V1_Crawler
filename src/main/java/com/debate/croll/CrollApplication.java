package com.debate.croll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class CrollApplication {

	public static void main(String[] args){

		SpringApplication.run(CrollApplication.class, args);

	}

}
