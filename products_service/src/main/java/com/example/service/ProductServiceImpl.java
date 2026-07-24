package com.example.service;

import com.example.common_lib.dto.GetProductDto;
import com.example.controller.dto.CreateOrUpdateProductDto;
import com.example.repository.ProductRepository;
import com.example.repository.entity.ProductEntity;
import com.example.utils.ProductEntityFilter;
import com.example.utils.ProductEntityMapper;
import com.example.common_lib.utils.exception.CommonConflictException;
import com.example.common_lib.utils.exception.CommonEntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductEntityMapper productEntityMapper;

    private final ProductRepository productRepository;

    private ProductEntity getRequiredProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() ->
                new CommonEntityNotFoundException("Product with id `%s` not found".formatted(id)));
    }

    private String normalizeName(String name) {
        return name.trim();
    }

    private String normalizeNameForUniqueCheck(String name) {
        return normalizeName(name).toLowerCase(Locale.ROOT);
    }

    private String normalizeDescription(String description) {
        return description.trim();
    }

    private void throwIfNameExists(String name, String normalizedName) {
        if (productRepository.existsByNormalizedName(normalizedName)) {
            throw new CommonConflictException("Product with name `%s` already exists".formatted(name));
        }
    }

    private void throwIfNameExistsForAnotherProduct(String name, String normalizedName, Long id) {
        if (productRepository.existsByNormalizedNameAndIdNot(normalizedName, id)) {
            throw new CommonConflictException("Product with name `%s` already exists".formatted(name));
        }
    }

    private void throwIfProductInactive(ProductEntity productEntity) {
        if (!Boolean.TRUE.equals(productEntity.getIsActive())) {
            throw new CommonConflictException("Product with id `%s` is inactive".formatted(productEntity.getId()));
        }
    }

    @Override
    public Page<GetProductDto> getAll(ProductEntityFilter filter, Pageable pageable) {
        Specification<ProductEntity> spec = filter.toSpecification();
        Page<ProductEntity> productEntities = productRepository.findAll(spec, pageable);
        return productEntities.map(productEntityMapper::convertToGetProductDto);
    }

    @Override
    public GetProductDto getOne(Long id) {
        ProductEntity entity = getRequiredProduct(id);
        return productEntityMapper.convertToGetProductDto(entity);
    }

    @Override
    public List<GetProductDto> getMany(List<Long> ids) {
        List<ProductEntity> productEntities = productRepository.findAllById(ids);
        return productEntities.stream().map(productEntityMapper::convertToGetProductDto).toList();
    }

    @Override
    public GetProductDto create(CreateOrUpdateProductDto dto) {
        String name = normalizeName(dto.name());
        String normalizedName = normalizeNameForUniqueCheck(dto.name());
        String normalizedDescription = normalizeDescription(dto.description());
        throwIfNameExists(name, normalizedName);
        ProductEntity productEntity = productEntityMapper.convertToEntity(dto);
        productEntity.setName(name);
        productEntity.setNormalizedName(normalizedName);
        productEntity.setDescription(normalizedDescription);
        productEntity.setIsActive(true);
        ProductEntity resultProductEntity = productRepository.save(productEntity);
        return productEntityMapper.convertToGetProductDto(resultProductEntity);
    }

    @Override
    public GetProductDto put(Long id, CreateOrUpdateProductDto dto) {
        ProductEntity productEntity = getRequiredProduct(id);
        String name = normalizeName(dto.name());
        String normalizedName = normalizeNameForUniqueCheck(dto.name());
        String normalizedDescription = normalizeDescription(dto.description());
        throwIfNameExistsForAnotherProduct(name, normalizedName, id);

        productEntityMapper.updateWithNull(dto, productEntity);
        productEntity.setName(name);
        productEntity.setNormalizedName(normalizedName);
        productEntity.setDescription(normalizedDescription);

        ProductEntity resultProductEntity = productRepository.save(productEntity);
        return productEntityMapper.convertToGetProductDto(resultProductEntity);
    }

    @Override
    public void setActiveProduct(Long id) {
        ProductEntity productEntity = getRequiredProduct(id);
        productEntity.setIsActive(true);
        productRepository.save(productEntity);
    }

    @Override
    public void setInactiveProduct(Long id) {
        ProductEntity productEntity = getRequiredProduct(id);
        productEntity.setIsActive(false);
        productRepository.save(productEntity);
    }

    @Override
    public void requireActiveProduct(ProductEntity productEntity) {
        throwIfProductInactive(productEntity);
    }

    @Override
    public ProductEntity getRequiredActiveProduct(Long id) {
        ProductEntity productEntity = getRequiredProduct(id);
        throwIfProductInactive(productEntity);
        return productEntity;
    }
}
