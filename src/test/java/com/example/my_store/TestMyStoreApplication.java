package com.example.my_store;

import org.springframework.boot.SpringApplication;

public class TestMyStoreApplication {

	public static void main(String[] args) {
		SpringApplication.from(MyStoreApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
