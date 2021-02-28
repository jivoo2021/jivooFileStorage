package com.palkar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.*")
public class JivooApplication {

	public static void main(String[] args) {
		SpringApplication.run(JivooApplication.class, args);
	}

}
