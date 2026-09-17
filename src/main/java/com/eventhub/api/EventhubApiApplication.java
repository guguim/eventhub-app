package com.eventhub.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class EventhubApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventhubApiApplication.class, args);
	}

}
