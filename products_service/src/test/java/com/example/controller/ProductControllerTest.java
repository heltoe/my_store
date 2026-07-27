package com.example.controller;

import com.example.controller.dto.CreateOrUpdateProductDto;
import com.example.common_lib.dto.GetProductDto;
import com.example.service.ProductService;
import com.example.utils.ProductEntityFilter;
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

@WebMvcTest(ProductController.class)
@Import(GlobalErrorHandler.class)
class ProductControllerTest {

    private static final Long PRODUCT_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private static GetProductDto sampleGetProductDto() {
        return new GetProductDto(
                new Date(),
                LocalDateTime.now(),
                PRODUCT_ID,
                "Ноутбук",
                "Игровой ноутбук",
                100_000.0,
                5,
                true
        );
    }

    private static String validProductJson() {
        return """
                {
                    "name": "Ноутбук",
                    "description": "Игровой ноутбук",
                    "price": 100000,
                    "quantity": 5
                }""";
    }

    private static String invalidProductJsonBlankName() {
        return """
                {
                    "name": "   ",
                    "description": "Игровой ноутбук",
                    "price": 100000,
                    "quantity": 5
                }""";
    }

    private static String invalidProductJsonNegativePrice() {
        return """
                {
                    "name": "Ноутбук",
                    "description": "Игровой ноутбук",
                    "price": -1,
                    "quantity": 5
                }""";
    }

    @Nested
    @DisplayName("GET /rest/products")
    class GetAllTests {

        @Test
        @DisplayName("возвращает 200 и постраничное тело")
        void getAll_returnsOkAndPagedBody() throws Exception {
            when(productService.getAll(any(ProductEntityFilter.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(sampleGetProductDto())));

            mockMvc.perform(get("/rest/products")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(PRODUCT_ID))
                    .andExpect(jsonPath("$.content[0].name").value("Ноутбук"))
                    .andExpect(jsonPath("$.content[0].isActive").value(true));
        }

        @Test
        @DisplayName("делегирует вызов в сервис с фильтром")
        void getAll_delegatesToService() throws Exception {
            when(productService.getAll(any(ProductEntityFilter.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(sampleGetProductDto())));

            mockMvc.perform(get("/rest/products")
                            .param("page", "0")
                            .param("size", "10")
                            .param("nameContains", "ноут")
                            .param("isActive", "false"))
                    .andExpect(status().isOk());

            verify(productService).getAll(any(ProductEntityFilter.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /rest/products/{id}")
    class GetOneTests {

        @Test
        @DisplayName("возвращает 200 и тело продукта")
        void getOne_returnsOkWithBody() throws Exception {
            when(productService.getOne(PRODUCT_ID)).thenReturn(sampleGetProductDto());

            mockMvc.perform(get("/rest/products/{id}", PRODUCT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(PRODUCT_ID))
                    .andExpect(jsonPath("$.isActive").value(true));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void getOne_delegatesToService() throws Exception {
            when(productService.getOne(PRODUCT_ID)).thenReturn(sampleGetProductDto());

            mockMvc.perform(get("/rest/products/{id}", PRODUCT_ID))
                    .andExpect(status().isOk());

            verify(productService).getOne(PRODUCT_ID);
        }
    }

    @Nested
    @DisplayName("GET /rest/products/by-ids")
    class GetManyTests {

        @Test
        @DisplayName("возвращает 200 и список продуктов")
        void getMany_returnsOkWithList() throws Exception {
            when(productService.getMany(List.of(1L, 2L))).thenReturn(List.of(sampleGetProductDto()));

            mockMvc.perform(get("/rest/products/by-ids")
                            .param("ids", "1", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(PRODUCT_ID));
        }
    }

    @Nested
    @DisplayName("POST /rest/products")
    class CreateTests {

        @Test
        @DisplayName("возвращает 201 и созданный продукт")
        void create_returnsCreatedWithBody() throws Exception {
            when(productService.create(any(CreateOrUpdateProductDto.class))).thenReturn(sampleGetProductDto());

            mockMvc.perform(post("/rest/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validProductJson()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(PRODUCT_ID))
                    .andExpect(jsonPath("$.quantity").value(5));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void create_delegatesToService() throws Exception {
            when(productService.create(any(CreateOrUpdateProductDto.class))).thenReturn(sampleGetProductDto());

            mockMvc.perform(post("/rest/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validProductJson()))
                    .andExpect(status().isCreated());

            verify(productService).create(any(CreateOrUpdateProductDto.class));
        }

        @Test
        @DisplayName("возвращает 400 при пустом name")
        void create_withBlankName_returnsBadRequest() throws Exception {
            mockMvc.perform(post("/rest/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidProductJsonBlankName()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.subErrors").isArray())
                    .andExpect(jsonPath("$.subErrors[0].field").value("name"));

            verify(productService, never()).create(any());
        }

        @Test
        @DisplayName("возвращает 400 при отрицательной цене")
        void create_withNegativePrice_returnsBadRequest() throws Exception {
            mockMvc.perform(post("/rest/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidProductJsonNegativePrice()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.subErrors").isArray())
                    .andExpect(jsonPath("$.subErrors[0].field").value("price"));

            verify(productService, never()).create(any());
        }
    }

    @Nested
    @DisplayName("PUT /rest/products/{id}")
    class PutTests {

        @Test
        @DisplayName("возвращает 200 и обновленный продукт")
        void put_returnsOkWithBody() throws Exception {
            when(productService.put(eq(PRODUCT_ID), any(CreateOrUpdateProductDto.class)))
                    .thenReturn(sampleGetProductDto());

            mockMvc.perform(put("/rest/products/{id}", PRODUCT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validProductJson()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(PRODUCT_ID));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void put_delegatesToService() throws Exception {
            when(productService.put(eq(PRODUCT_ID), any(CreateOrUpdateProductDto.class)))
                    .thenReturn(sampleGetProductDto());

            mockMvc.perform(put("/rest/products/{id}", PRODUCT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validProductJson()))
                    .andExpect(status().isOk());

            verify(productService).put(eq(PRODUCT_ID), any(CreateOrUpdateProductDto.class));
        }
    }

    @Nested
    @DisplayName("POST /rest/products/{id}/set-inactive")
    class SetInactiveTests {

        @Test
        @DisplayName("возвращает 204")
        void setInactiveProduct_returnsNoContent() throws Exception {
            mockMvc.perform(post("/rest/products/{id}/set-inactive", PRODUCT_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void setInactiveProduct_delegatesToService() throws Exception {
            mockMvc.perform(post("/rest/products/{id}/set-inactive", PRODUCT_ID))
                    .andExpect(status().isNoContent());

            verify(productService).setInactiveProduct(PRODUCT_ID);
        }
    }

    @Nested
    @DisplayName("POST /rest/products/{id}/set-active")
    class SetActiveTests {

        @Test
        @DisplayName("возвращает 204")
        void setActiveProduct_returnsNoContent() throws Exception {
            mockMvc.perform(post("/rest/products/{id}/set-active", PRODUCT_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void setActiveProduct_delegatesToService() throws Exception {
            mockMvc.perform(post("/rest/products/{id}/set-active", PRODUCT_ID))
                    .andExpect(status().isNoContent());

            verify(productService).setActiveProduct(PRODUCT_ID);
        }
    }
}
