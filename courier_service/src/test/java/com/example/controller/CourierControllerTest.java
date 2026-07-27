package com.example.controller;

import com.example.controller.dto.CreateUpdateCourierDto;
import com.example.common_lib.dto.GetCourierDto;
import com.example.service.CourierService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourierController.class)
@Import(GlobalErrorHandler.class)
class CourierControllerTest {

    private static final Long COURIER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourierService courierService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private static GetCourierDto sampleGetCourierDto() {
        return new GetCourierDto(
                new Date(),
                LocalDateTime.now(),
                COURIER_ID,
                "Владислав",
                "Сергеевич",
                "Жулинский",
                "+79255702395",
                true
        );
    }

    private static String validCourierJson() {
        return """
                {
                    "name": "Владислав",
                    "secondName": "Сергеевич",
                    "lastName": "Жулинский",
                    "phoneNumber": "+79255702395"
                }""";
    }

    private static String invalidCourierJson() {
        return """
                {
                    "name": "Владислав",
                    "secondName": "Сергеевич",
                    "lastName": "Жулинский",
                    "phoneNumber": "123"
                }""";
    }

    @Nested
    @DisplayName("GET /rest/couriers")
    class GetAllTests {

        @Test
        @DisplayName("возвращает 200 и постраничное тело")
        void getAll_returnsOkAndPagedBody() throws Exception {
            // Arrange: сервис возвращает страницу с одним курьером.
            when(courierService.getAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(sampleGetCourierDto())));

            // Act + Assert: выполняем GET-запрос и проверяем HTTP-статус и тело ответа.
            mockMvc.perform(get("/rest/couriers")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(COURIER_ID))
                    .andExpect(jsonPath("$.content[0].phoneNumber").value("+79255702395"))
                    .andExpect(jsonPath("$.content[0].isActive").value(true));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void getAll_delegatesToService() throws Exception {
            // Arrange: сервис возвращает корректный ответ для списка курьеров.
            when(courierService.getAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(sampleGetCourierDto())));

            // Act: выполняем GET-запрос с параметрами пагинации.
            mockMvc.perform(get("/rest/couriers")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk());

            // Assert: контроллер делегировал обработку в service-слой.
            verify(courierService).getAll(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /rest/couriers/{id}")
    class GetOneTests {

        @Test
        @DisplayName("возвращает 200 и тело курьера")
        void getOne_returnsOkWithBody() throws Exception {
            // Arrange: сервис возвращает курьера по id.
            when(courierService.getOne(COURIER_ID)).thenReturn(sampleGetCourierDto());

            // Act + Assert: выполняем GET-запрос и проверяем тело ответа.
            mockMvc.perform(get("/rest/couriers/{id}", COURIER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(COURIER_ID))
                    .andExpect(jsonPath("$.name").value("Владислав"));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void getOne_delegatesToService() throws Exception {
            // Arrange: сервис возвращает курьера по id.
            when(courierService.getOne(COURIER_ID)).thenReturn(sampleGetCourierDto());

            // Act: выполняем GET-запрос по id.
            mockMvc.perform(get("/rest/couriers/{id}", COURIER_ID))
                    .andExpect(status().isOk());

            // Assert: контроллер вызвал нужный метод сервиса.
            verify(courierService).getOne(COURIER_ID);
        }
    }

    @Nested
    @DisplayName("GET /rest/couriers/by-ids")
    class GetManyTests {

        @Test
        @DisplayName("возвращает 200 и список курьеров")
        void getMany_returnsOkWithList() throws Exception {
            // Arrange: сервис возвращает список курьеров по id.
            when(courierService.getMany(List.of(1L, 2L))).thenReturn(List.of(sampleGetCourierDto()));

            // Act + Assert: выполняем GET-запрос и проверяем список в ответе.
            mockMvc.perform(get("/rest/couriers/by-ids")
                            .param("ids", "1", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(COURIER_ID));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void getMany_delegatesToService() throws Exception {
            // Arrange: сервис возвращает список курьеров по id.
            when(courierService.getMany(List.of(1L, 2L))).thenReturn(List.of(sampleGetCourierDto()));

            // Act: выполняем GET-запрос со списком id.
            mockMvc.perform(get("/rest/couriers/by-ids")
                            .param("ids", "1", "2"))
                    .andExpect(status().isOk());

            // Assert: контроллер передал список id в service-слой.
            verify(courierService).getMany(List.of(1L, 2L));
        }
    }

    @Nested
    @DisplayName("POST /rest/couriers")
    class CreateTests {

        @Test
        @DisplayName("возвращает 201 и созданного курьера")
        void create_returnsCreatedWithBody() throws Exception {
            // Arrange: сервис возвращает созданного курьера.
            when(courierService.create(any(CreateUpdateCourierDto.class))).thenReturn(sampleGetCourierDto());

            // Act + Assert: отправляем валидный POST-запрос и проверяем ответ.
            mockMvc.perform(post("/rest/couriers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validCourierJson()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(COURIER_ID))
                    .andExpect(jsonPath("$.lastName").value("Жулинский"));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void create_delegatesToService() throws Exception {
            // Arrange: сервис возвращает созданного курьера.
            when(courierService.create(any(CreateUpdateCourierDto.class))).thenReturn(sampleGetCourierDto());

            // Act: отправляем валидный POST-запрос.
            mockMvc.perform(post("/rest/couriers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validCourierJson()))
                    .andExpect(status().isCreated());

            // Assert: контроллер передал DTO в service-слой.
            verify(courierService).create(any(CreateUpdateCourierDto.class));
        }

        @Test
        @DisplayName("возвращает 400 при невалидном теле запроса")
        void create_withInvalidBody_returnsBadRequest() throws Exception {
            // Act + Assert: отправляем невалидный POST-запрос и проверяем ошибку валидации.
            mockMvc.perform(post("/rest/couriers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidCourierJson()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.subErrors").isArray())
                    .andExpect(jsonPath("$.subErrors[0].field").value("phoneNumber"));

            // Assert: при ошибке валидации service-слой не вызывается.
            verify(courierService, never()).create(any());
        }
    }

    @Nested
    @DisplayName("PUT /rest/couriers/{id}")
    class PatchTests {

        @Test
        @DisplayName("возвращает 200 и обновленного курьера")
        void patch_returnsOkWithBody() throws Exception {
            // Arrange: сервис возвращает обновленного курьера.
            when(courierService.patch(eq(COURIER_ID), any(CreateUpdateCourierDto.class)))
                    .thenReturn(sampleGetCourierDto());

            // Act + Assert: отправляем валидный PUT-запрос и проверяем ответ.
            mockMvc.perform(put("/rest/couriers/{id}", COURIER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validCourierJson()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(COURIER_ID));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void patch_delegatesToService() throws Exception {
            // Arrange: сервис возвращает обновленного курьера.
            when(courierService.patch(eq(COURIER_ID), any(CreateUpdateCourierDto.class)))
                    .thenReturn(sampleGetCourierDto());

            // Act: отправляем валидный PUT-запрос.
            mockMvc.perform(put("/rest/couriers/{id}", COURIER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validCourierJson()))
                    .andExpect(status().isOk());

            // Assert: контроллер передал id и DTO в service-слой.
            verify(courierService).patch(eq(COURIER_ID), any(CreateUpdateCourierDto.class));
        }

        @Test
        @DisplayName("возвращает 400 при невалидном теле запроса")
        void patch_withInvalidBody_returnsBadRequest() throws Exception {
            // Act + Assert: отправляем невалидный PUT-запрос и проверяем ошибку валидации.
            mockMvc.perform(put("/rest/couriers/{id}", COURIER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidCourierJson()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.subErrors").isArray())
                    .andExpect(jsonPath("$.subErrors[0].field").value("phoneNumber"));

            // Assert: при ошибке валидации service-слой не вызывается.
            verify(courierService, never()).patch(any(), any());
        }
    }

    @Nested
    @DisplayName("POST /rest/couriers/{id}/set-inactive")
    class SetInactiveTests {

        @Test
        @DisplayName("возвращает 204")
        void setInactiveCourier_returnsNoContent() throws Exception {
            // Act + Assert: выполняем POST-запрос и проверяем статус без тела.
            mockMvc.perform(post("/rest/couriers/{id}/set-inactive", COURIER_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void setInactiveCourier_delegatesToService() throws Exception {
            // Act: выполняем POST-запрос по id курьера.
            mockMvc.perform(post("/rest/couriers/{id}/set-inactive", COURIER_ID))
                    .andExpect(status().isNoContent());

            // Assert: контроллер передал id в service-слой.
            verify(courierService).setInactiveCourier(COURIER_ID);
        }
    }

    @Nested
    @DisplayName("POST /rest/couriers/{id}/set-active")
    class SetActiveTests {

        @Test
        @DisplayName("возвращает 204")
        void setActiveCourier_returnsNoContent() throws Exception {
            // Act + Assert: выполняем POST-запрос и проверяем статус без тела.
            mockMvc.perform(post("/rest/couriers/{id}/set-active", COURIER_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void setActiveCourier_delegatesToService() throws Exception {
            // Act: выполняем POST-запрос по id курьера.
            mockMvc.perform(post("/rest/couriers/{id}/set-active", COURIER_ID))
                    .andExpect(status().isNoContent());

            // Assert: контроллер передал id в service-слой.
            verify(courierService).setActiveCourier(COURIER_ID);
        }
    }
}
