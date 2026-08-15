package com.priye.streamvault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StreamVaultApplication {

	public static void main(String[] args) {
		SpringApplication.run(StreamVaultApplication.class, args);
	}

}
