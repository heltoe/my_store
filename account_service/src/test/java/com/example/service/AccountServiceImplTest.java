package com.example.service;

import com.example.controller.dto.CreateOrUpdateAccountDto;
import com.example.common_lib.dto.GetAccountDto;
import com.example.repository.AccountRepository;
import com.example.repository.entity.AccountEntity;
import com.example.utils.AccountEntityFilter;
import com.example.utils.AccountEntityMapper;
import com.example.common_lib.utils.exception.CommonEntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    private static final Long ACCOUNT_ID = 1L;

    @Mock
    private AccountEntityMapper accountEntityMapper;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private static AccountEntity accountEntity() {
        AccountEntity entity = new AccountEntity();
        entity.setId(ACCOUNT_ID);
        entity.setPhoneNumber("+79255702395");
        entity.setFirstName("Владислав");
        entity.setSecondName("Сергеевич");
        entity.setLastName("Жулинский");
        return entity;
    }

    private static GetAccountDto getAccountDto() {
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

    private static CreateOrUpdateAccountDto createOrUpdateDto() {
        return new CreateOrUpdateAccountDto(
                "+79255702395",
                "Владислав",
                "Сергеевич",
                "Жулинский"
        );
    }

    @Test
    @DisplayName("getAll возвращает страницу аккаунтов, преобразованную в DTO")
    void getAll_returnsMappedPage() {
        // Arrange: готовим входные данные и поведение зависимостей.
        AccountEntity entity = accountEntity();
        GetAccountDto dto = getAccountDto();
        Pageable pageable = PageRequest.of(0, 10);
        AccountEntityFilter filter = new AccountEntityFilter("925", null, null, null);

        when(accountRepository.findAll(anySpecification(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(accountEntityMapper.convertToGetAccountDto(entity)).thenReturn(dto);

        // Act: вызываем метод сервиса, который тестируем.
        var result = accountService.getAll(filter, pageable);

        // Assert: проверяем результат и нужные вызовы зависимостей.
        assertThat(result.getContent()).containsExactly(dto);
        verify(accountRepository).findAll(anySpecification(), any(Pageable.class));
        verify(accountEntityMapper).convertToGetAccountDto(entity);
    }

    private static Specification<AccountEntity> anySpecification() {
        return any();
    }

    @Test
    @DisplayName("getOne возвращает DTO, если аккаунт найден")
    void getOne_whenAccountExists_returnsDto() {
        // Arrange: репозиторий находит аккаунт, mapper превращает entity в DTO.
        AccountEntity entity = accountEntity();
        GetAccountDto dto = getAccountDto();

        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(entity));
        when(accountEntityMapper.convertToGetAccountDto(entity)).thenReturn(dto);

        // Act: запрашиваем аккаунт по id.
        GetAccountDto result = accountService.getOne(ACCOUNT_ID);

        // Assert: сервис вернул DTO и обратился к нужным зависимостям.
        assertThat(result).isEqualTo(dto);
        verify(accountRepository).findById(ACCOUNT_ID);
        verify(accountEntityMapper).convertToGetAccountDto(entity);
    }

    @Test
    @DisplayName("getOne бросает 404, если аккаунт не найден")
    void getOne_whenAccountDoesNotExist_throwsNotFound() {
        // Arrange: репозиторий не находит аккаунт.
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act + Assert: вызов сервиса должен завершиться 404 ошибкой.
        assertThatThrownBy(() -> accountService.getOne(ACCOUNT_ID))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Entity with id `1` not found");

        // Assert: сервис действительно пытался найти аккаунт по id.
        verify(accountRepository).findById(ACCOUNT_ID);
    }

    @Test
    @DisplayName("getMany возвращает список аккаунтов, преобразованный в DTO")
    void getMany_returnsMappedDtos() {
        // Arrange: репозиторий возвращает список entity, mapper превращает их в DTO.
        AccountEntity entity = accountEntity();
        GetAccountDto dto = getAccountDto();
        List<Long> ids = List.of(ACCOUNT_ID);

        when(accountRepository.findAllById(ids)).thenReturn(List.of(entity));
        when(accountEntityMapper.convertToGetAccountDto(entity)).thenReturn(dto);

        // Act: запрашиваем несколько аккаунтов по id.
        List<GetAccountDto> result = accountService.getMany(ids);

        // Assert: получили ожидаемый список DTO и проверили вызовы.
        assertThat(result).containsExactly(dto);
        verify(accountRepository).findAllById(ids);
        verify(accountEntityMapper).convertToGetAccountDto(entity);
    }

    @Test
    @DisplayName("create сохраняет новый аккаунт и возвращает DTO")
    void create_savesEntityAndReturnsDto() {
        // Arrange: mapper создает entity из DTO, repository сохраняет ее.
        CreateOrUpdateAccountDto requestDto = createOrUpdateDto();
        AccountEntity entity = accountEntity();
        GetAccountDto responseDto = getAccountDto();

        when(accountEntityMapper.convertToEntity(requestDto)).thenReturn(entity);
        when(accountRepository.save(entity)).thenReturn(entity);
        when(accountEntityMapper.convertToGetAccountDto(entity)).thenReturn(responseDto);

        // Act: создаем аккаунт через сервис.
        GetAccountDto result = accountService.create(requestDto);

        // Assert: сервис вернул DTO и выполнил цепочку mapper -> repository -> mapper.
        assertThat(result).isEqualTo(responseDto);
        verify(accountEntityMapper).convertToEntity(requestDto);
        verify(accountRepository).save(entity);
        verify(accountEntityMapper).convertToGetAccountDto(entity);
    }

    @Test
    @DisplayName("put обновляет существующий аккаунт и возвращает DTO")
    void put_updatesExistingEntityAndReturnsDto() {
        // Arrange: существующий аккаунт найден, сохранение возвращает обновленную entity.
        CreateOrUpdateAccountDto requestDto = createOrUpdateDto();
        AccountEntity entity = accountEntity();
        GetAccountDto responseDto = getAccountDto();

        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(entity));
        when(accountRepository.save(entity)).thenReturn(entity);
        when(accountEntityMapper.convertToGetAccountDto(entity)).thenReturn(responseDto);

        // Act: обновляем аккаунт через сервис.
        GetAccountDto result = accountService.put(ACCOUNT_ID, requestDto);

        // Assert: сервис обновил найденную entity, сохранил ее и вернул DTO.
        assertThat(result).isEqualTo(responseDto);
        verify(accountRepository).findById(ACCOUNT_ID);
        verify(accountEntityMapper).updateWithNull(requestDto, entity);
        verify(accountRepository).save(entity);
        verify(accountEntityMapper).convertToGetAccountDto(entity);
    }

    @Test
    @DisplayName("delete удаляет аккаунт, если он найден")
    void delete_whenAccountExists_deletesEntity() {
        // Arrange: репозиторий находит аккаунт для удаления.
        AccountEntity entity = accountEntity();

        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(entity));

        // Act: удаляем аккаунт через сервис.
        accountService.delete(ACCOUNT_ID);

        // Assert: сервис нашел entity и передал ее в delete.
        verify(accountRepository).findById(ACCOUNT_ID);
        verify(accountRepository).delete(entity);
    }

    @Test
    @DisplayName("deleteMany удаляет аккаунты по списку id")
    void deleteMany_deletesByIds() {
        // Arrange: готовим список id для массового удаления.
        List<Long> ids = List.of(1L, 2L);

        // Act: удаляем несколько аккаунтов через сервис.
        accountService.deleteMany(ids);

        // Assert: сервис делегировал удаление репозиторию.
        verify(accountRepository).deleteAllById(ids);
    }
}
