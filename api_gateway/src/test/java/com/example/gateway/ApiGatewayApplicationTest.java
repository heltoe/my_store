package com.example.gateway;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ApiGatewayApplicationTest {

    @Test
    @SuppressWarnings("unchecked")
    void applicationYamlDefinesSevenRoutes() {
        InputStream input = getClass().getResourceAsStream("/application.yml");
        assertThat(input).isNotNull();

        Map<String, Object> config = new Yaml().load(input);
        Map<String, Object> spring = (Map<String, Object>) config.get("spring");
        Map<String, Object> cloud = (Map<String, Object>) spring.get("cloud");
        Map<String, Object> gateway = (Map<String, Object>) cloud.get("gateway");
        Map<String, Object> server = (Map<String, Object>) gateway.get("server");
        Map<String, Object> webflux = (Map<String, Object>) server.get("webflux");
        List<Map<String, Object>> routes = (List<Map<String, Object>>) webflux.get("routes");

        List<String> routeIds = routes.stream()
                .map(route -> (String) route.get("id"))
                .collect(Collectors.toList());
        List<String> predicates = routes.stream()
                .map(route -> (String) ((List<?>) route.get("predicates")).get(0))
                .collect(Collectors.toList());

        assertThat(routeIds).containsExactlyInAnyOrder(
                "account-service",
                "products-service",
                "cart-service",
                "order-service",
                "payment-service",
                "courier-service",
                "delivery-service"
        );
        assertThat(predicates).containsExactlyInAnyOrder(
                "Path=/rest/accounts/**",
                "Path=/rest/products/**",
                "Path=/rest/carts/**",
                "Path=/rest/orders/**",
                "Path=/rest/payments/**",
                "Path=/rest/couriers/**",
                "Path=/rest/deliveries/**"
        );
    }
}
