package com.project.cache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class CacheApplication {

	public static void main(String[] args) {

		//SpringApplication.run(CacheApplication.class, args);
		ConfigurableApplicationContext context = SpringApplication.run(CacheApplication.class, args);
		Environment env = context.getEnvironment();

		System.out.println("=== PROPERTY TEST ===");
		System.out.println("server.port: " + env.getProperty("server.port"));
		System.out.println("spring.application.name: " + env.getProperty("spring.application.name"));
		System.out.println("spring.datasource.url: " + env.getProperty("spring.datasource.url"));
		System.out.println("===================");
	}

}
