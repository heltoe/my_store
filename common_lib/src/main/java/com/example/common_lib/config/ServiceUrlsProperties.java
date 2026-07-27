package com.example.common_lib.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "services")
public class ServiceUrlsProperties {
    private String account;
    private String products;
    private String cart;
    private String order;
    private String payment;
    private String courier;
    private String delivery;
}
