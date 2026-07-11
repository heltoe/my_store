package com.example.my_store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MyStoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyStoreApplication.class, args);
	}

}
