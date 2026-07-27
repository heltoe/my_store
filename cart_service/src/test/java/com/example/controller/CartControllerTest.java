package com.example.controller;

import com.example.controller.dto.cart.CreateCartDto;
import com.example.controller.dto.cart.GetCartDto;
import com.example.controller.dto.cart_item.CreateCartItemDto;
import com.example.controller.dto.cart_item.UpdateCartItemDto;
import com.example.service.CartService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@Import(GlobalErrorHandler.class)
class CartControllerTest {

    private static final Long CART_ID = 1L;
    private static final Long CART_ITEM_ID = 10L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private static GetCartDto sampleGetCartDto() {
        return new GetCartDto(
                new Date(),
                LocalDateTime.now(),
                CART_ID
        );
    }

    private static String validCreateCartJson() {
        return """
                {
                    "account_id": 1
                }""";
    }

    private static String invalidCreateCartJson() {
        return """
                {
                    "account_id": null
                }""";
    }

    private static String validCreateCartItemJson() {
        return """
                {
                    "cart_id": 1,
                    "product_id": 2,
                    "quantity": 3
                }""";
    }

    private static String invalidCreateCartItemJson() {
        return """
                {
                    "cart_id": 1,
                    "product_id": 2,
                    "quantity": 0
                }""";
    }

    private static String validUpdateCartItemJson() {
        return """
                {
                    "id": 10,
                    "quantity": 5
                }""";
    }

    private static String invalidUpdateCartItemJson() {
        return """
                {
                    "id": 10,
                    "quantity": -1
                }""";
    }

    @Nested
    @DisplayName("GET /rest/carts")
    class GetAllTests {

        @Test
        @DisplayName("возвращает 200 и постраничное тело")
        void getAll_returnsOkAndPagedBody() throws Exception {
            // Arrange: сервис возвращает страницу с одной корзиной.
            when(cartService.getAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(sampleGetCartDto())));

            // Act + Assert: выполняем GET-запрос и проверяем HTTP-статус и тело ответа.
            mockMvc.perform(get("/rest/carts")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(CART_ID));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void getAll_delegatesToService() throws Exception {
            // Arrange: сервис возвращает корректный ответ для списка корзин.
            when(cartService.getAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(sampleGetCartDto())));

            // Act: выполняем GET-запрос с параметрами пагинации.
            mockMvc.perform(get("/rest/carts")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk());

            // Assert: контроллер делегировал обработку в service-слой.
            verify(cartService).getAll(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /rest/carts/{id}")
    class GetOneTests {

        @Test
        @DisplayName("возвращает 200 и тело корзины")
        void getOne_returnsOkWithBody() throws Exception {
            // Arrange: сервис возвращает корзину по id.
            when(cartService.getOne(CART_ID)).thenReturn(sampleGetCartDto());

            // Act + Assert: выполняем GET-запрос и проверяем тело ответа.
            mockMvc.perform(get("/rest/carts/{id}", CART_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(CART_ID));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void getOne_delegatesToService() throws Exception {
            // Arrange: сервис возвращает корзину по id.
            when(cartService.getOne(CART_ID)).thenReturn(sampleGetCartDto());

            // Act: выполняем GET-запрос по id.
            mockMvc.perform(get("/rest/carts/{id}", CART_ID))
                    .andExpect(status().isOk());

            // Assert: контроллер вызвал нужный метод сервиса.
            verify(cartService).getOne(CART_ID);
        }
    }

    @Nested
    @DisplayName("GET /rest/carts/by-ids")
    class GetManyTests {

        @Test
        @DisplayName("возвращает 200 и список корзин")
        void getMany_returnsOkWithList() throws Exception {
            // Arrange: сервис возвращает список корзин по id.
            when(cartService.getMany(List.of(1L, 2L))).thenReturn(List.of(sampleGetCartDto()));

            // Act + Assert: выполняем GET-запрос и проверяем список в ответе.
            mockMvc.perform(get("/rest/carts/by-ids")
                            .param("ids", "1", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(CART_ID));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void getMany_delegatesToService() throws Exception {
            // Arrange: сервис возвращает список корзин по id.
            when(cartService.getMany(List.of(1L, 2L))).thenReturn(List.of(sampleGetCartDto()));

            // Act: выполняем GET-запрос со списком id.
            mockMvc.perform(get("/rest/carts/by-ids")
                            .param("ids", "1", "2"))
                    .andExpect(status().isOk());

            // Assert: контроллер передал список id в service-слой.
            verify(cartService).getMany(List.of(1L, 2L));
        }
    }

    @Nested
    @DisplayName("POST /rest/carts")
    class CreateTests {

        @Test
        @DisplayName("возвращает 201 и созданную корзину")
        void create_returnsCreatedWithBody() throws Exception {
            // Arrange: сервис возвращает созданную корзину.
            when(cartService.create(any(CreateCartDto.class))).thenReturn(sampleGetCartDto());

            // Act + Assert: отправляем валидный POST-запрос и проверяем ответ.
            mockMvc.perform(post("/rest/carts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validCreateCartJson()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(CART_ID));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void create_delegatesToService() throws Exception {
            // Arrange: сервис возвращает созданную корзину.
            when(cartService.create(any(CreateCartDto.class))).thenReturn(sampleGetCartDto());

            // Act: отправляем валидный POST-запрос.
            mockMvc.perform(post("/rest/carts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validCreateCartJson()))
                    .andExpect(status().isCreated());

            // Assert: контроллер передал DTO в service-слой.
            verify(cartService).create(any(CreateCartDto.class));
        }

        @Test
        @DisplayName("возвращает 400 при невалидном теле запроса")
        void create_withInvalidBody_returnsBadRequest() throws Exception {
            // Act + Assert: отправляем невалидный POST-запрос и проверяем ошибку валидации.
            mockMvc.perform(post("/rest/carts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidCreateCartJson()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.subErrors").isArray())
                    .andExpect(jsonPath("$.subErrors[0].field").value("account_id"));

            // Assert: при ошибке валидации service-слой не вызывается.
            verify(cartService, never()).create(any());
        }
    }

    @Nested
    @DisplayName("POST /rest/carts/add")
    class AddToCartTests {

        @Test
        @DisplayName("возвращает 204")
        void addToCart_returnsNoContent() throws Exception {
            // Act + Assert: отправляем валидный POST-запрос и проверяем статус без тела.
            mockMvc.perform(post("/rest/carts/add")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validCreateCartItemJson()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void addToCart_delegatesToService() throws Exception {
            // Act: отправляем валидный POST-запрос для добавления товара.
            mockMvc.perform(post("/rest/carts/add")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validCreateCartItemJson()))
                    .andExpect(status().isNoContent());

            // Assert: контроллер передал DTO в service-слой.
            verify(cartService).addToCart(any(CreateCartItemDto.class));
        }

        @Test
        @DisplayName("возвращает 400 при невалидном количестве")
        void addToCart_withInvalidQuantity_returnsBadRequest() throws Exception {
            // Act + Assert: отправляем невалидное количество и проверяем ошибку валидации.
            mockMvc.perform(post("/rest/carts/add")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidCreateCartItemJson()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.subErrors").isArray())
                    .andExpect(jsonPath("$.subErrors[0].field").value("quantity"));

            // Assert: при ошибке валидации service-слой не вызывается.
            verify(cartService, never()).addToCart(any());
        }
    }

    @Nested
    @DisplayName("POST /rest/carts/change-quantity")
    class ChangeQuantityTests {

        @Test
        @DisplayName("возвращает 204")
        void changeQuantity_returnsNoContent() throws Exception {
            // Act + Assert: отправляем валидный POST-запрос и проверяем статус без тела.
            mockMvc.perform(post("/rest/carts/change-quantity")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validUpdateCartItemJson()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void changeQuantity_delegatesToService() throws Exception {
            // Act: отправляем валидный POST-запрос для изменения количества.
            mockMvc.perform(post("/rest/carts/change-quantity")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validUpdateCartItemJson()))
                    .andExpect(status().isNoContent());

            // Assert: контроллер передал DTO в service-слой.
            verify(cartService).changeQuantity(any(UpdateCartItemDto.class));
        }

        @Test
        @DisplayName("возвращает 400 при отрицательном количестве")
        void changeQuantity_withInvalidQuantity_returnsBadRequest() throws Exception {
            // Act + Assert: отправляем отрицательное количество и проверяем ошибку валидации.
            mockMvc.perform(post("/rest/carts/change-quantity")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidUpdateCartItemJson()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.subErrors").isArray())
                    .andExpect(jsonPath("$.subErrors[0].field").value("quantity"));

            // Assert: при ошибке валидации service-слой не вызывается.
            verify(cartService, never()).changeQuantity(any());
        }
    }

    @Nested
    @DisplayName("DELETE /rest/carts/remove/{id}")
    class RemoveFromCartTests {

        @Test
        @DisplayName("возвращает 204")
        void removeFromCart_returnsNoContent() throws Exception {
            // Act + Assert: выполняем DELETE-запрос и проверяем статус без тела.
            mockMvc.perform(delete("/rest/carts/remove/{id}", CART_ITEM_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void removeFromCart_delegatesToService() throws Exception {
            // Act: выполняем DELETE-запрос по id позиции корзины.
            mockMvc.perform(delete("/rest/carts/remove/{id}", CART_ITEM_ID))
                    .andExpect(status().isNoContent());

            // Assert: контроллер передал id в service-слой.
            verify(cartService).removeFromCart(CART_ITEM_ID);
        }
    }

    @Nested
    @DisplayName("DELETE /rest/carts")
    class RemoveManyFromCartTests {

        @Test
        @DisplayName("возвращает 204")
        void removeManyFromCart_returnsNoContent() throws Exception {
            // Act + Assert: выполняем DELETE-запрос со списком id и проверяем статус.
            mockMvc.perform(delete("/rest/carts")
                            .param("ids", "10", "11"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void removeManyFromCart_delegatesToService() throws Exception {
            // Act: выполняем DELETE-запрос со списком id.
            mockMvc.perform(delete("/rest/carts")
                            .param("ids", "10", "11"))
                    .andExpect(status().isNoContent());

            // Assert: контроллер передал список id в service-слой.
            verify(cartService).removeManyFromCart(List.of(10L, 11L));
        }
    }
}
