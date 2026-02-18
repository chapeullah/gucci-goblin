package com.chapeullah.GucciGoblin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GucciGoblinApplication {
	public static void main(String[] args) {
		SpringApplication.run(GucciGoblinApplication.class, args);
	}
}