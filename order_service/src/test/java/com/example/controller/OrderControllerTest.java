package com.example.controller;

import com.example.common_lib.dto.GetOrderDto;
import com.example.common_lib.dto.STATE_ORDER;
import com.example.common_lib.utils.exception.GlobalErrorHandler;
import com.example.controller.dto.BaseCreateOrderDto;
import com.example.controller.dto.ChangeOrderStatusDto;
import com.example.controller.dto.CreateOrderDto;
import com.example.controller.dto.CreateOrderItemDto;
import com.example.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(GlobalErrorHandler.class)
class OrderControllerTest {

    private static final Long ORDER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private static GetOrderDto sampleGetOrderDto() {
        return new GetOrderDto(
                new Date(),
                LocalDateTime.now(),
                ORDER_ID,
                STATE_ORDER.CREATED
        );
    }

    private static String validCreateOrderJson() {
        return """
                {
                    "orderDto": {
                        "accountId": 2
                    },
                    "items": [
                        {
                            "productId": 3,
                            "quantity": 2
                        }
                    ]
                }""";
    }

    private static String invalidCreateOrderJson() {
        return """
                {
                    "orderDto": {
                        "accountId": null
                    },
                    "items": [
                        {
                            "productId": 3,
                            "quantity": 2
                        }
                    ]
                }""";
    }

    private static String validChangeStatusJson() {
        return """
                {
                    "status": "ACCEPTED"
                }""";
    }

    @Nested
    @DisplayName("GET /rest/orders")
    class GetAllTests {

        @Test
        @DisplayName("возвращает 200 и постраничное тело")
        void getAll_returnsOkAndPagedBody() throws Exception {
            when(orderService.getAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(sampleGetOrderDto())));

            mockMvc.perform(get("/rest/orders")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(ORDER_ID))
                    .andExpect(jsonPath("$.content[0].stateOrder").value("CREATED"));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void getAll_delegatesToService() throws Exception {
            when(orderService.getAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(sampleGetOrderDto())));

            mockMvc.perform(get("/rest/orders")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk());

            verify(orderService).getAll(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /rest/orders/{id}")
    class GetOneTests {

        @Test
        @DisplayName("возвращает 200 и тело заказа")
        void getOne_returnsOkWithBody() throws Exception {
            when(orderService.getOne(ORDER_ID)).thenReturn(sampleGetOrderDto());

            mockMvc.perform(get("/rest/orders/{id}", ORDER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ORDER_ID))
                    .andExpect(jsonPath("$.stateOrder").value("CREATED"));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void getOne_delegatesToService() throws Exception {
            when(orderService.getOne(ORDER_ID)).thenReturn(sampleGetOrderDto());

            mockMvc.perform(get("/rest/orders/{id}", ORDER_ID))
                    .andExpect(status().isOk());

            verify(orderService).getOne(ORDER_ID);
        }
    }

    @Nested
    @DisplayName("GET /rest/orders/by-ids")
    class GetManyTests {

        @Test
        @DisplayName("возвращает 200 и список заказов")
        void getMany_returnsOkWithList() throws Exception {
            when(orderService.getMany(List.of(1L, 2L))).thenReturn(List.of(sampleGetOrderDto()));

            mockMvc.perform(get("/rest/orders/by-ids")
                            .param("ids", "1", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(ORDER_ID));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void getMany_delegatesToService() throws Exception {
            when(orderService.getMany(List.of(1L, 2L))).thenReturn(List.of(sampleGetOrderDto()));

            mockMvc.perform(get("/rest/orders/by-ids")
                            .param("ids", "1", "2"))
                    .andExpect(status().isOk());

            verify(orderService).getMany(List.of(1L, 2L));
        }
    }

    @Nested
    @DisplayName("POST /rest/orders")
    class CreateTests {

        @Test
        @DisplayName("возвращает 201 и созданный заказ")
        void create_returnsCreatedWithBody() throws Exception {
            when(orderService.create(any(CreateOrderDto.class))).thenReturn(sampleGetOrderDto());

            mockMvc.perform(post("/rest/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validCreateOrderJson()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(ORDER_ID));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void create_delegatesToService() throws Exception {
            when(orderService.create(any(CreateOrderDto.class))).thenReturn(sampleGetOrderDto());

            mockMvc.perform(post("/rest/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validCreateOrderJson()))
                    .andExpect(status().isCreated());

            verify(orderService).create(any(CreateOrderDto.class));
        }

        @Test
        @DisplayName("возвращает 400 при невалидном теле запроса")
        void create_withInvalidBody_returnsBadRequest() throws Exception {
            mockMvc.perform(post("/rest/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidCreateOrderJson()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.subErrors").isArray())
                    .andExpect(jsonPath("$.subErrors[0].field").value("orderDto.accountId"));

            verify(orderService, never()).create(any());
        }
    }

    @Nested
    @DisplayName("PATCH /rest/orders/{id}/change-status")
    class ChangeStatusTests {

        @Test
        @DisplayName("возвращает 204")
        void changeOrderState_returnsNoContent() throws Exception {
            mockMvc.perform(patch("/rest/orders/{id}/change-status", ORDER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validChangeStatusJson()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void changeOrderState_delegatesToService() throws Exception {
            mockMvc.perform(patch("/rest/orders/{id}/change-status", ORDER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validChangeStatusJson()))
                    .andExpect(status().isNoContent());

            verify(orderService).changeOrderState(eq(ORDER_ID), eq(STATE_ORDER.ACCEPTED));
        }
    }
}
