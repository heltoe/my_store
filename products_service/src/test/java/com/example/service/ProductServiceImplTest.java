package com.example.service;

import com.example.common_lib.dto.GetProductDto;
import com.example.common_lib.utils.exception.CommonConflictException;
import com.example.common_lib.utils.exception.CommonEntityNotFoundException;
import com.example.controller.dto.CreateOrUpdateProductDto;
import com.example.repository.ProductRepository;
import com.example.repository.entity.ProductEntity;
import com.example.utils.ProductEntityFilter;
import com.example.utils.ProductEntityMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    private static final Long PRODUCT_ID = 1L;
    private static final String PRODUCT_NAME = "Ноутбук";
    private static final String DUPLICATE_NAME = "  ноутбук  ";

    @Mock
    private ProductEntityMapper productEntityMapper;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private static ProductEntity productEntity() {
        ProductEntity product = new ProductEntity();
        product.setId(PRODUCT_ID);
        product.setName(PRODUCT_NAME);
        product.setNormalizedName("ноутбук");
        product.setDescription("Игровой ноутбук");
        product.setPrice(100_000.0);
        product.setQuantity(5);
        product.setIsActive(true);
        return product;
    }

    private static GetProductDto getProductDto() {
        return new GetProductDto(
                new Date(),
                LocalDateTime.now(),
                PRODUCT_ID,
                PRODUCT_NAME,
                "Игровой ноутбук",
                100_000.0,
                5,
                true
        );
    }

    private static CreateOrUpdateProductDto createOrUpdateDto() {
        return new CreateOrUpdateProductDto(
                PRODUCT_NAME,
                "Игровой ноутбук",
                100_000.0,
                5
        );
    }

    private static CreateOrUpdateProductDto createOrUpdateDtoWithName(String name) {
        return new CreateOrUpdateProductDto(
                name,
                "Игровой ноутбук",
                100_000.0,
                5
        );
    }

    @Test
    @DisplayName("getAll возвращает страницу продуктов, преобразованную в DTO")
    void getAll_returnsMappedPage() {
        ProductEntity entity = productEntity();
        GetProductDto dto = getProductDto();
        ProductEntityFilter filter = new ProductEntityFilter(null, null);
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(productEntityMapper.convertToGetProductDto(entity)).thenReturn(dto);

        var result = productService.getAll(filter, pageable);

        assertThat(result.getContent()).containsExactly(dto);
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
        verify(productEntityMapper).convertToGetProductDto(entity);
    }

    @Test
    @DisplayName("getOne возвращает DTO, если продукт найден")
    void getOne_whenProductExists_returnsDto() {
        ProductEntity entity = productEntity();
        GetProductDto dto = getProductDto();

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(entity));
        when(productEntityMapper.convertToGetProductDto(entity)).thenReturn(dto);

        GetProductDto result = productService.getOne(PRODUCT_ID);

        assertThat(result).isEqualTo(dto);
        verify(productRepository).findById(PRODUCT_ID);
    }

    @Test
    @DisplayName("getOne бросает 404, если продукт не найден")
    void getOne_whenProductDoesNotExist_throwsNotFound() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getOne(PRODUCT_ID))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Product with id `1` not found");
    }

    @Test
    @DisplayName("getMany возвращает список продуктов, преобразованный в DTO")
    void getMany_returnsMappedDtos() {
        ProductEntity entity = productEntity();
        GetProductDto dto = getProductDto();
        List<Long> ids = List.of(PRODUCT_ID);

        when(productRepository.findAllById(ids)).thenReturn(List.of(entity));
        when(productEntityMapper.convertToGetProductDto(entity)).thenReturn(dto);

        List<GetProductDto> result = productService.getMany(ids);

        assertThat(result).containsExactly(dto);
        verify(productRepository).findAllById(ids);
    }

    @Test
    @DisplayName("create сохраняет активный продукт с нормализованным именем")
    void create_savesActiveProductWithNormalizedName() {
        CreateOrUpdateProductDto requestDto = createOrUpdateDto();
        ProductEntity entity = new ProductEntity();
        GetProductDto responseDto = getProductDto();

        when(productRepository.existsByNormalizedName("ноутбук")).thenReturn(false);
        when(productEntityMapper.convertToEntity(requestDto)).thenReturn(entity);
        when(productRepository.save(entity)).thenReturn(entity);
        when(productEntityMapper.convertToGetProductDto(entity)).thenReturn(responseDto);

        GetProductDto result = productService.create(requestDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(entity.getName()).isEqualTo(PRODUCT_NAME);
        assertThat(entity.getNormalizedName()).isEqualTo("ноутбук");
        assertThat(entity.getDescription()).isEqualTo("Игровой ноутбук");
        assertThat(entity.getIsActive()).isTrue();
        verify(productRepository).existsByNormalizedName("ноутбук");
        verify(productRepository).save(entity);
    }

    @Test
    @DisplayName("create бросает 409 при дубликате имени в другом регистре")
    void create_whenNameExistsInDifferentCase_throwsConflict() {
        when(productRepository.existsByNormalizedName("ноутбук")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(createOrUpdateDtoWithName(DUPLICATE_NAME)))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Product with name `ноутбук` already exists");

        verify(productRepository).existsByNormalizedName("ноутбук");
    }

    @Test
    @DisplayName("put обновляет продукт, если имя не занято другим продуктом")
    void put_whenNameIsAvailable_updatesProduct() {
        ProductEntity entity = productEntity();
        CreateOrUpdateProductDto requestDto = createOrUpdateDtoWithName("  Монитор  ");
        GetProductDto responseDto = getProductDto();

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(entity));
        when(productRepository.existsByNormalizedNameAndIdNot("монитор", PRODUCT_ID)).thenReturn(false);
        when(productRepository.save(entity)).thenReturn(entity);
        when(productEntityMapper.convertToGetProductDto(entity)).thenReturn(responseDto);

        GetProductDto result = productService.put(PRODUCT_ID, requestDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(entity.getName()).isEqualTo("Монитор");
        assertThat(entity.getNormalizedName()).isEqualTo("монитор");
        verify(productEntityMapper).updateWithNull(requestDto, entity);
        verify(productRepository).save(entity);
    }

    @Test
    @DisplayName("put бросает 409, если имя занято другим продуктом")
    void put_whenNameBelongsToAnotherProduct_throwsConflict() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(productEntity()));
        when(productRepository.existsByNormalizedNameAndIdNot("мышь", PRODUCT_ID)).thenReturn(true);

        assertThatThrownBy(() -> productService.put(PRODUCT_ID, createOrUpdateDtoWithName("Мышь")))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Product with name `Мышь` already exists");
    }

    @Test
    @DisplayName("setActiveProduct активирует найденный продукт")
    void setActiveProduct_setsActive() {
        ProductEntity entity = productEntity();
        entity.setIsActive(false);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(entity));

        productService.setActiveProduct(PRODUCT_ID);

        assertThat(entity.getIsActive()).isTrue();
        verify(productRepository).save(entity);
    }

    @Test
    @DisplayName("setInactiveProduct деактивирует продукт")
    void setInactiveProduct_setsInactive() {
        ProductEntity entity = productEntity();
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(entity));

        productService.setInactiveProduct(PRODUCT_ID);

        assertThat(entity.getIsActive()).isFalse();
        verify(productRepository).save(entity);
    }

    @Test
    @DisplayName("requireActiveProduct не бросает исключение для активного продукта")
    void requireActiveProduct_whenProductIsActive_doesNotThrow() {
        ProductEntity entity = productEntity();

        productService.requireActiveProduct(entity);
    }

    @Test
    @DisplayName("requireActiveProduct бросает 409 для неактивного продукта")
    void requireActiveProduct_whenProductIsInactive_throwsConflict() {
        ProductEntity entity = productEntity();
        entity.setIsActive(false);

        assertThatThrownBy(() -> productService.requireActiveProduct(entity))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Product with id `1` is inactive");
    }

    @Test
    @DisplayName("getRequiredActiveProduct возвращает активный продукт")
    void getRequiredActiveProduct_whenProductIsActive_returnsProduct() {
        ProductEntity entity = productEntity();
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(entity));

        ProductEntity result = productService.getRequiredActiveProduct(PRODUCT_ID);

        assertThat(result).isEqualTo(entity);
    }

    @Test
    @DisplayName("getRequiredActiveProduct бросает 409 для неактивного продукта")
    void getRequiredActiveProduct_whenProductIsInactive_throwsConflict() {
        ProductEntity entity = productEntity();
        entity.setIsActive(false);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> productService.getRequiredActiveProduct(PRODUCT_ID))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Product with id `1` is inactive");
    }

    @Test
    @DisplayName("create проверяет уникальность по normalizedName через ArgumentCaptor")
    void create_usesNormalizedNameForDuplicateCheck() {
        CreateOrUpdateProductDto requestDto = createOrUpdateDtoWithName("  НОУТБУК  ");
        ProductEntity entity = new ProductEntity();

        when(productRepository.existsByNormalizedName("ноутбук")).thenReturn(false);
        when(productEntityMapper.convertToEntity(requestDto)).thenReturn(entity);
        when(productRepository.save(entity)).thenReturn(entity);
        when(productEntityMapper.convertToGetProductDto(entity)).thenReturn(getProductDto());

        productService.create(requestDto);

        ArgumentCaptor<String> normalizedNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(productRepository).existsByNormalizedName(normalizedNameCaptor.capture());
        assertThat(normalizedNameCaptor.getValue()).isEqualTo("ноутбук");
        assertThat(entity.getNormalizedName()).isEqualTo("ноутбук");
        assertThat(entity.getName()).isEqualTo("НОУТБУК");
    }
}
