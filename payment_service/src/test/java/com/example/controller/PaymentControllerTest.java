package com.example.controller;

import com.example.controller.dto.CreatePaymentDto;
import com.example.controller.dto.GetPaymentDto;
import com.example.repository.entity.STATE_PAYMENT;
import com.example.service.PaymentService;
import com.example.common_lib.utils.exception.GlobalErrorHandler;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(GlobalErrorHandler.class)
class PaymentControllerTest {

    private static final Long PAYMENT_ID = 1L;
    private static final Long ORDER_ID = 2L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private static GetPaymentDto sampleGetPaymentDto(STATE_PAYMENT status) {
        return new GetPaymentDto(
                new Date(),
                LocalDateTime.now(),
                PAYMENT_ID,
                ORDER_ID,
                status
        );
    }

    private static String validCreatePaymentJson() {
        return """
                {
                    "orderId": 2
                }""";
    }

    private static String invalidCreatePaymentJson() {
        return """
                {
                    "orderId": null
                }""";
    }

    @Nested
    @DisplayName("GET /rest/payments")
    class GetAllTests {

        @Test
        @DisplayName("возвращает 200 и постраничное тело")
        void getAll_returnsOkAndPagedBody() throws Exception {
            when(paymentService.getAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(sampleGetPaymentDto(STATE_PAYMENT.PENDING))));

            mockMvc.perform(get("/rest/payments")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(PAYMENT_ID))
                    .andExpect(jsonPath("$.content[0].orderId").value(ORDER_ID))
                    .andExpect(jsonPath("$.content[0].status").value("PENDING"));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void getAll_delegatesToService() throws Exception {
            when(paymentService.getAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(sampleGetPaymentDto(STATE_PAYMENT.PENDING))));

            mockMvc.perform(get("/rest/payments")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk());

            verify(paymentService).getAll(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /rest/payments/{id}")
    class GetOneTests {

        @Test
        @DisplayName("возвращает 200 и тело платежа")
        void getOne_returnsOkWithBody() throws Exception {
            when(paymentService.getOne(PAYMENT_ID)).thenReturn(sampleGetPaymentDto(STATE_PAYMENT.PENDING));

            mockMvc.perform(get("/rest/payments/{id}", PAYMENT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(PAYMENT_ID))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void getOne_delegatesToService() throws Exception {
            when(paymentService.getOne(PAYMENT_ID)).thenReturn(sampleGetPaymentDto(STATE_PAYMENT.PENDING));

            mockMvc.perform(get("/rest/payments/{id}", PAYMENT_ID))
                    .andExpect(status().isOk());

            verify(paymentService).getOne(PAYMENT_ID);
        }
    }

    @Nested
    @DisplayName("GET /rest/payments/by-order/{orderId}")
    class GetByOrderIdTests {

        @Test
        @DisplayName("возвращает 200 и тело платежа")
        void getByOrderId_returnsOkWithBody() throws Exception {
            when(paymentService.getByOrderId(ORDER_ID)).thenReturn(sampleGetPaymentDto(STATE_PAYMENT.PENDING));

            mockMvc.perform(get("/rest/payments/by-order/{orderId}", ORDER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value(ORDER_ID))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void getByOrderId_delegatesToService() throws Exception {
            when(paymentService.getByOrderId(ORDER_ID)).thenReturn(sampleGetPaymentDto(STATE_PAYMENT.PENDING));

            mockMvc.perform(get("/rest/payments/by-order/{orderId}", ORDER_ID))
                    .andExpect(status().isOk());

            verify(paymentService).getByOrderId(ORDER_ID);
        }
    }

    @Nested
    @DisplayName("GET /rest/payments/by-ids")
    class GetManyTests {

        @Test
        @DisplayName("возвращает 200 и список платежей")
        void getMany_returnsOkWithList() throws Exception {
            when(paymentService.getMany(List.of(1L, 2L))).thenReturn(List.of(sampleGetPaymentDto(STATE_PAYMENT.PENDING)));

            mockMvc.perform(get("/rest/payments/by-ids")
                            .param("ids", "1", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(PAYMENT_ID));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void getMany_delegatesToService() throws Exception {
            when(paymentService.getMany(List.of(1L, 2L))).thenReturn(List.of(sampleGetPaymentDto(STATE_PAYMENT.PENDING)));

            mockMvc.perform(get("/rest/payments/by-ids")
                            .param("ids", "1", "2"))
                    .andExpect(status().isOk());

            verify(paymentService).getMany(List.of(1L, 2L));
        }
    }

    @Nested
    @DisplayName("POST /rest/payments")
    class CreateTests {

        @Test
        @DisplayName("возвращает 201 и созданный платеж")
        void create_returnsCreatedWithBody() throws Exception {
            when(paymentService.create(any(CreatePaymentDto.class)))
                    .thenReturn(sampleGetPaymentDto(STATE_PAYMENT.PENDING));

            mockMvc.perform(post("/rest/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validCreatePaymentJson()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(PAYMENT_ID))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void create_delegatesToService() throws Exception {
            when(paymentService.create(any(CreatePaymentDto.class)))
                    .thenReturn(sampleGetPaymentDto(STATE_PAYMENT.PENDING));

            mockMvc.perform(post("/rest/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validCreatePaymentJson()))
                    .andExpect(status().isCreated());

            verify(paymentService).create(any(CreatePaymentDto.class));
        }

        @Test
        @DisplayName("возвращает 400 при отсутствии orderId")
        void create_withNullOrderId_returnsBadRequest() throws Exception {
            mockMvc.perform(post("/rest/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidCreatePaymentJson()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.subErrors").isArray())
                    .andExpect(jsonPath("$.subErrors[0].field").value("orderId"));

            verify(paymentService, never()).create(any());
        }
    }

    @Nested
    @DisplayName("PATCH /rest/payments/{id}/mark-success")
    class MarkSuccessTests {

        @Test
        @DisplayName("возвращает 200 и обновленный платеж")
        void markSuccess_returnsOkWithBody() throws Exception {
            when(paymentService.markSuccess(PAYMENT_ID)).thenReturn(sampleGetPaymentDto(STATE_PAYMENT.SUCCESS));

            mockMvc.perform(patch("/rest/payments/{id}/mark-success", PAYMENT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(PAYMENT_ID))
                    .andExpect(jsonPath("$.status").value("SUCCESS"));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void markSuccess_delegatesToService() throws Exception {
            when(paymentService.markSuccess(PAYMENT_ID)).thenReturn(sampleGetPaymentDto(STATE_PAYMENT.SUCCESS));

            mockMvc.perform(patch("/rest/payments/{id}/mark-success", PAYMENT_ID))
                    .andExpect(status().isOk());

            verify(paymentService).markSuccess(PAYMENT_ID);
        }
    }

    @Nested
    @DisplayName("PATCH /rest/payments/{id}/mark-failure")
    class MarkFailureTests {

        @Test
        @DisplayName("возвращает 200 и обновленный платеж")
        void markFailure_returnsOkWithBody() throws Exception {
            when(paymentService.markFailure(PAYMENT_ID)).thenReturn(sampleGetPaymentDto(STATE_PAYMENT.FAILURE));

            mockMvc.perform(patch("/rest/payments/{id}/mark-failure", PAYMENT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(PAYMENT_ID))
                    .andExpect(jsonPath("$.status").value("FAILURE"));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void markFailure_delegatesToService() throws Exception {
            when(paymentService.markFailure(PAYMENT_ID)).thenReturn(sampleGetPaymentDto(STATE_PAYMENT.FAILURE));

            mockMvc.perform(patch("/rest/payments/{id}/mark-failure", PAYMENT_ID))
                    .andExpect(status().isOk());

            verify(paymentService).markFailure(PAYMENT_ID);
        }
    }
}
