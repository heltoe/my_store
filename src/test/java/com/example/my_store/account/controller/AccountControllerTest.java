package com.example.my_store.account.controller;

import com.example.my_store.account.utils.AccountEntityFilter;
import com.example.my_store.account.controller.dto.CreateOrUpdateAccountDto;
import com.example.my_store.account.controller.dto.GetAccountDto;
import com.example.my_store.account.service.AccountService;
import com.example.my_store.utils.exception.GlobalErrorHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import(GlobalErrorHandler.class)
class AccountControllerTest {

    private static final Long ACCOUNT_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    private static GetAccountDto sampleGetAccountDto() {
        return new GetAccountDto(
                new Date(),
                LocalDateTime.now(),
                ACCOUNT_ID,
                "+79255702395",
                "Владислав",
                "Сергеевич",
                "Жулинский"
        );
    }

    private static String validAccountJson() {
        return """
                {
                    "phoneNumber": "+79255702395",
                    "firstName": "Владислав",
                    "secondName": "Сергеевич",
                    "lastName": "Жулинский"
                }""";
    }

    private static String invalidAccountJson() {
        return """
                {
                    "phoneNumber": "123",
                    "firstName": "Владислав",
                    "secondName": "Сергеевич",
                    "lastName": "Жулинский"
                }""";
    }

    @Nested
    @DisplayName("GET /rest/accounts")
    class GetAllTests {

        @Test
        @DisplayName("возвращает 200 и постраничное тело")
        void getAll_returnsOkAndPagedBody() throws Exception {
            GetAccountDto dto = sampleGetAccountDto();
            when(accountService.getAll(any(AccountEntityFilter.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(dto)));

            mockMvc.perform(get("/rest/accounts")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(ACCOUNT_ID))
                    .andExpect(jsonPath("$.content[0].phoneNumber").value("+79255702395"));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void getAll_delegatesToService() throws Exception {
            when(accountService.getAll(any(AccountEntityFilter.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(sampleGetAccountDto())));

            mockMvc.perform(get("/rest/accounts")
                            .param("phoneNumberContains", "925")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk());

            verify(accountService).getAll(any(AccountEntityFilter.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /rest/accounts/{id}")
    class GetOneTests {

        @Test
        @DisplayName("возвращает 200 и тело аккаунта")
        void getOne_returnsOkWithBody() throws Exception {
            when(accountService.getOne(ACCOUNT_ID)).thenReturn(sampleGetAccountDto());

            mockMvc.perform(get("/rest/accounts/{id}", ACCOUNT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ACCOUNT_ID))
                    .andExpect(jsonPath("$.phoneNumber").value("+79255702395"));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void getOne_delegatesToService() throws Exception {
            when(accountService.getOne(ACCOUNT_ID)).thenReturn(sampleGetAccountDto());

            mockMvc.perform(get("/rest/accounts/{id}", ACCOUNT_ID))
                    .andExpect(status().isOk());

            verify(accountService).getOne(ACCOUNT_ID);
        }
    }

    @Nested
    @DisplayName("GET /rest/accounts/by-ids")
    class GetManyTests {

        @Test
        @DisplayName("возвращает 200 и список аккаунтов")
        void getMany_returnsOkWithList() throws Exception {
            when(accountService.getMany(List.of(1L, 2L))).thenReturn(List.of(sampleGetAccountDto()));

            mockMvc.perform(get("/rest/accounts/by-ids")
                            .param("ids", "1", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(ACCOUNT_ID));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void getMany_delegatesToService() throws Exception {
            when(accountService.getMany(List.of(1L, 2L))).thenReturn(List.of(sampleGetAccountDto()));

            mockMvc.perform(get("/rest/accounts/by-ids")
                            .param("ids", "1", "2"))
                    .andExpect(status().isOk());

            verify(accountService).getMany(List.of(1L, 2L));
        }
    }

    @Nested
    @DisplayName("POST /rest/accounts")
    class CreateTests {

        @Test
        @DisplayName("возвращает 201 и созданный аккаунт")
        void create_returnsCreatedWithBody() throws Exception {
            when(accountService.create(any(CreateOrUpdateAccountDto.class))).thenReturn(sampleGetAccountDto());

            mockMvc.perform(post("/rest/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validAccountJson()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(ACCOUNT_ID))
                    .andExpect(jsonPath("$.firstName").value("Владислав"));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void create_delegatesToService() throws Exception {
            when(accountService.create(any(CreateOrUpdateAccountDto.class))).thenReturn(sampleGetAccountDto());

            mockMvc.perform(post("/rest/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validAccountJson()))
                    .andExpect(status().isCreated());

            verify(accountService).create(any(CreateOrUpdateAccountDto.class));
        }

        @Test
        @DisplayName("возвращает 400 при невалидном теле запроса")
        void create_withInvalidBody_returnsBadRequest() throws Exception {
            mockMvc.perform(post("/rest/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidAccountJson()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.subErrors").isArray())
                    .andExpect(jsonPath("$.subErrors[0].field").value("phoneNumber"));

            verify(accountService, never()).create(any());
        }
    }

    @Nested
    @DisplayName("PUT /rest/accounts/{id}")
    class PatchTests {

        @Test
        @DisplayName("возвращает 200 и обновлённый аккаунт")
        void patch_returnsOkWithBody() throws Exception {
            when(accountService.put(eq(ACCOUNT_ID), any(CreateOrUpdateAccountDto.class)))
                    .thenReturn(sampleGetAccountDto());

            mockMvc.perform(put("/rest/accounts/{id}", ACCOUNT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validAccountJson()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ACCOUNT_ID));
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void patch_delegatesToService() throws Exception {
            when(accountService.put(eq(ACCOUNT_ID), any(CreateOrUpdateAccountDto.class)))
                    .thenReturn(sampleGetAccountDto());

            mockMvc.perform(put("/rest/accounts/{id}", ACCOUNT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validAccountJson()))
                    .andExpect(status().isOk());

            verify(accountService).put(eq(ACCOUNT_ID), any(CreateOrUpdateAccountDto.class));
        }

        @Test
        @DisplayName("возвращает 400 при невалидном теле запроса")
        void patch_withInvalidBody_returnsBadRequest() throws Exception {
            mockMvc.perform(put("/rest/accounts/{id}", ACCOUNT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidAccountJson()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.subErrors").isArray())
                    .andExpect(jsonPath("$.subErrors[0].field").value("phoneNumber"));

            verify(accountService, never()).put(any(), any());
        }
    }

    @Nested
    @DisplayName("DELETE /rest/accounts/{id}")
    class DeleteTests {

        @Test
        @DisplayName("возвращает 204")
        void delete_returnsNoContent() throws Exception {
            mockMvc.perform(delete("/rest/accounts/{id}", ACCOUNT_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void delete_delegatesToService() throws Exception {
            mockMvc.perform(delete("/rest/accounts/{id}", ACCOUNT_ID))
                    .andExpect(status().isNoContent());

            verify(accountService).delete(ACCOUNT_ID);
        }
    }

    @Nested
    @DisplayName("DELETE /rest/accounts")
    class DeleteManyTests {

        @Test
        @DisplayName("возвращает 204")
        void deleteMany_returnsNoContent() throws Exception {
            mockMvc.perform(delete("/rest/accounts")
                            .param("ids", "1", "2"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("делегирует вызов в сервис")
        void deleteMany_delegatesToService() throws Exception {
            mockMvc.perform(delete("/rest/accounts")
                            .param("ids", "1", "2"))
                    .andExpect(status().isNoContent());

            verify(accountService).deleteMany(List.of(1L, 2L));
        }
    }
}
