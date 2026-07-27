package com.example.controller;

import com.example.controller.dto.CreateDeliveryDto;
import com.example.common_lib.dto.GetDeliveryDto;
import com.example.controller.dto.UpdateDeliveryDto;
import com.example.service.DeliveryService;
import com.example.utils.DeliveryEntityFilter;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeliveryController.class)
@Import(GlobalErrorHandler.class)
class DeliveryControllerTest {

    private static final Long DELIVERY_ID = 1L;
    private static final Long ORDER_ID = 2L;
    private static final Long COURIER_ID = 3L;
    private static final String FUTURE_DATE = "2030-01-01T10:00:00";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeliveryService deliveryService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private static GetDeliveryDto sampleGetDeliveryDto() {
        return new GetDeliveryDto(
                new Date(),
                LocalDateTime.now(),
                DELIVERY_ID,
                ORDER_ID,
                COURIER_ID,
                LocalDateTime.parse(FUTURE_DATE),
                "Москва",
                "Оставить у двери",
                55.75,
                37.61
        );
    }

    private static String validCreateDeliveryJson() {
        return """
                {
                    "orderId": 2,
                    "courierId": 3,
                    "deliveryDate": "2030-01-01T10:00:00",
                    "deliveryPlace": "Москва",
                    "description": "Оставить у двери",
                    "lat": 55.75,
                    "lon": 37.61
                }""";
    }

    private static String validUpdateDeliveryJson() {
        return """
                {
                    "courierId": 3,
                    "deliveryDate": "2030-01-01T10:00:00",
                    "deliveryPlace": "Москва",
                    "description": "Оставить у двери",
                    "lat": 55.75,
                    "lon": 37.61
                }""";
    }

    private static String invalidDeliveryJson() {
        return """
                {
                    "orderId": 2,
                    "courierId": 3,
                    "deliveryDate": "2030-01-01T10:00:00",
                    "deliveryPlace": "Москва",
                    "description": "Оставить у двери",
                    "lat": 91.0,
                    "lon": 37.61
                }""";
    }

    @Nested
    @DisplayName("GET /rest/deliveries")
    class GetAllTests {

        @Test
        @DisplayName("возвращает 200 и постраничное тело")
        void getAll_returnsOkAndPagedBody() throws Exception {
            // Arrange: сервис возвращает страницу с одной доставкой.
            when(deliveryService.getAll(any(DeliveryEntityFilter.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(sampleGetDeliveryDto())));

            // Act + Assert: выполняем GET-запрос и проверяем HTTP-статус и тело ответа.
            mockMvc.perform(get("/rest/deliveries")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(DELIVERY_ID))
                    .andExpect(jsonPath("$.content[0].orderId").value(ORDER_ID))
                    .andExpect(jsonPath("$.content[0].courierId").value(COURIER_ID));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void getAll_delegatesToService() throws Exception {
            // Arrange: сервис возвращает корректный ответ для списка доставок.
            when(deliveryService.getAll(any(DeliveryEntityFilter.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(sampleGetDeliveryDto())));

            // Act: выполняем GET-запрос с фильтром и параметрами пагинации.
            mockMvc.perform(get("/rest/deliveries")
                            .param("id", "1")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk());

            // Assert: контроллер делегировал обработку в service-слой.
            verify(deliveryService).getAll(any(DeliveryEntityFilter.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /rest/deliveries/{id}")
    class GetOneTests {

        @Test
        @DisplayName("возвращает 200 и тело доставки")
        void getOne_returnsOkWithBody() throws Exception {
            // Arrange: сервис возвращает доставку по id.
            when(deliveryService.getOne(DELIVERY_ID)).thenReturn(sampleGetDeliveryDto());

            // Act + Assert: выполняем GET-запрос и проверяем тело ответа.
            mockMvc.perform(get("/rest/deliveries/{id}", DELIVERY_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(DELIVERY_ID))
                    .andExpect(jsonPath("$.deliveryPlace").value("Москва"));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void getOne_delegatesToService() throws Exception {
            // Arrange: сервис возвращает доставку по id.
            when(deliveryService.getOne(DELIVERY_ID)).thenReturn(sampleGetDeliveryDto());

            // Act: выполняем GET-запрос по id.
            mockMvc.perform(get("/rest/deliveries/{id}", DELIVERY_ID))
                    .andExpect(status().isOk());

            // Assert: контроллер вызвал нужный метод сервиса.
            verify(deliveryService).getOne(DELIVERY_ID);
        }
    }

    @Nested
    @DisplayName("GET /rest/deliveries/by-ids")
    class GetManyTests {

        @Test
        @DisplayName("возвращает 200 и список доставок")
        void getMany_returnsOkWithList() throws Exception {
            // Arrange: сервис возвращает список доставок по id.
            when(deliveryService.getMany(List.of(1L, 2L))).thenReturn(List.of(sampleGetDeliveryDto()));

            // Act + Assert: выполняем GET-запрос и проверяем список в ответе.
            mockMvc.perform(get("/rest/deliveries/by-ids")
                            .param("ids", "1", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(DELIVERY_ID));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void getMany_delegatesToService() throws Exception {
            // Arrange: сервис возвращает список доставок по id.
            when(deliveryService.getMany(List.of(1L, 2L))).thenReturn(List.of(sampleGetDeliveryDto()));

            // Act: выполняем GET-запрос со списком id.
            mockMvc.perform(get("/rest/deliveries/by-ids")
                            .param("ids", "1", "2"))
                    .andExpect(status().isOk());

            // Assert: контроллер передал список id в service-слой.
            verify(deliveryService).getMany(List.of(1L, 2L));
        }
    }

    @Nested
    @DisplayName("POST /rest/deliveries")
    class CreateTests {

        @Test
        @DisplayName("возвращает 201 и созданную доставку")
        void create_returnsCreatedWithBody() throws Exception {
            // Arrange: сервис возвращает созданную доставку.
            when(deliveryService.create(any(CreateDeliveryDto.class))).thenReturn(sampleGetDeliveryDto());

            // Act + Assert: отправляем валидный POST-запрос и проверяем ответ.
            mockMvc.perform(post("/rest/deliveries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validCreateDeliveryJson()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(DELIVERY_ID))
                    .andExpect(jsonPath("$.deliveryPlace").value("Москва"));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void create_delegatesToService() throws Exception {
            // Arrange: сервис возвращает созданную доставку.
            when(deliveryService.create(any(CreateDeliveryDto.class))).thenReturn(sampleGetDeliveryDto());

            // Act: отправляем валидный POST-запрос.
            mockMvc.perform(post("/rest/deliveries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validCreateDeliveryJson()))
                    .andExpect(status().isCreated());

            // Assert: контроллер передал DTO в service-слой.
            verify(deliveryService).create(any(CreateDeliveryDto.class));
        }

        @Test
        @DisplayName("возвращает 400 при невалидной широте")
        void create_withInvalidLat_returnsBadRequest() throws Exception {
            // Act + Assert: отправляем невалидный POST-запрос и проверяем ошибку валидации.
            mockMvc.perform(post("/rest/deliveries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidDeliveryJson()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.subErrors").isArray())
                    .andExpect(jsonPath("$.subErrors[0].field").value("lat"));

            // Assert: при ошибке валидации service-слой не вызывается.
            verify(deliveryService, never()).create(any());
        }
    }

    @Nested
    @DisplayName("PATCH /rest/deliveries/{id}")
    class PatchTests {

        @Test
        @DisplayName("возвращает 200 и обновленную доставку")
        void patch_returnsOkWithBody() throws Exception {
            // Arrange: сервис возвращает обновленную доставку.
            when(deliveryService.patch(eq(DELIVERY_ID), any(UpdateDeliveryDto.class)))
                    .thenReturn(sampleGetDeliveryDto());

            // Act + Assert: отправляем валидный PATCH-запрос и проверяем ответ.
            mockMvc.perform(patch("/rest/deliveries/{id}", DELIVERY_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validUpdateDeliveryJson()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(DELIVERY_ID));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void patch_delegatesToService() throws Exception {
            // Arrange: сервис возвращает обновленную доставку.
            when(deliveryService.patch(eq(DELIVERY_ID), any(UpdateDeliveryDto.class)))
                    .thenReturn(sampleGetDeliveryDto());

            // Act: отправляем валидный PATCH-запрос.
            mockMvc.perform(patch("/rest/deliveries/{id}", DELIVERY_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validUpdateDeliveryJson()))
                    .andExpect(status().isOk());

            // Assert: контроллер передал id и DTO в service-слой.
            verify(deliveryService).patch(eq(DELIVERY_ID), any(UpdateDeliveryDto.class));
        }

        @Test
        @DisplayName("возвращает 400 при невалидной широте")
        void patch_withInvalidLat_returnsBadRequest() throws Exception {
            // Act + Assert: отправляем невалидный PATCH-запрос и проверяем ошибку валидации.
            mockMvc.perform(patch("/rest/deliveries/{id}", DELIVERY_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidDeliveryJson()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.subErrors").isArray())
                    .andExpect(jsonPath("$.subErrors[0].field").value("lat"));

            // Assert: при ошибке валидации service-слой не вызывается.
            verify(deliveryService, never()).patch(any(), any());
        }
    }

    @Nested
    @DisplayName("DELETE /rest/deliveries/{id}")
    class DeleteTests {

        @Test
        @DisplayName("возвращает 204")
        void delete_returnsNoContent() throws Exception {
            // Act + Assert: выполняем DELETE-запрос и проверяем статус без тела.
            mockMvc.perform(delete("/rest/deliveries/{id}", DELIVERY_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void delete_delegatesToService() throws Exception {
            // Act: выполняем DELETE-запрос по id.
            mockMvc.perform(delete("/rest/deliveries/{id}", DELIVERY_ID))
                    .andExpect(status().isNoContent());

            // Assert: контроллер передал id в service-слой.
            verify(deliveryService).delete(DELIVERY_ID);
        }
    }
}
