package com.example.rssFeedv2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class RssFeedv2Application {

	public static void main(String[] args) {
		SpringApplication.run(RssFeedv2Application.class, args);
	}

}
