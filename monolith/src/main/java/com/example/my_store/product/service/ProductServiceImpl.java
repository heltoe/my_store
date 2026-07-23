package com.example.my_store.product.service;

import com.example.my_store.cart.repository.cart_item.CartItemRepository;
import com.example.my_store.order.repository.order.entity.STATE_ORDER;
import com.example.my_store.order.repository.order_item.OrderItemRepository;
import com.example.my_store.product.controller.dto.CreateOrUpdateProductDto;
import com.example.my_store.product.controller.dto.GetProductDto;
import com.example.my_store.product.utils.ProductEntityFilter;
import com.example.my_store.product.repository.entity.ProductEntity;
import com.example.my_store.product.repository.ProductRepository;
import com.example.my_store.product.utils.ProductEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.example.my_store.utils.exception.CommonConflictException;
import com.example.my_store.utils.exception.CommonEntityNotFoundException;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductEntityMapper productEntityMapper;

    private final ProductRepository productRepository;

    private final CartItemRepository cartItemRepository;

    private final OrderItemRepository orderItemRepository;

    private static final List<STATE_ORDER> TERMINAL_ORDER_STATUSES = List.of(
            STATE_ORDER.RECEIVED_BY_USER,
            STATE_ORDER.CANCELED
    );

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

    private void throwIfProductInCart(Long productId) {
        if (cartItemRepository.existsByProduct_Id(productId)) {
            throw new CommonConflictException("Product with id `%s` is in cart".formatted(productId));
        }
    }

    private void throwIfProductInActiveOrder(Long productId) {
        if (orderItemRepository.existsByProduct_IdAndOrder_StatusNotIn(productId, TERMINAL_ORDER_STATUSES)) {
            throw new CommonConflictException("Product with id `%s` is in active order".formatted(productId));
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
        throwIfProductInCart(id);
        throwIfProductInActiveOrder(id);
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
