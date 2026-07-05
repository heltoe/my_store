package com.example.my_store.product.service;

import com.example.my_store.product.controller.dto.CreateOrUdpateProductDto;
import com.example.my_store.product.controller.dto.GetProductDto;
import com.example.my_store.product.utils.ProductEntityFilter;
import com.example.my_store.product.repository.entity.ProductEntity;
import com.example.my_store.product.repository.ProductRepository;
import com.example.my_store.product.utils.ProductEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductEntityMapper productEntityMapper;

    private final ProductRepository productRepository;

    public ProductEntity _getOne(Long id) {
        return productRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
    }

    @Override
    public Page<GetProductDto> getAll(ProductEntityFilter filter, Pageable pageable) {
        Specification<ProductEntity> spec = filter.toSpecification();
        Page<ProductEntity> productEntities = productRepository.findAll(spec, pageable);
        return productEntities.map(productEntityMapper::convertToGetProductDto);
    }

    @Override
    public GetProductDto getOne(Long id) {
        ProductEntity entity = _getOne(id);
        return productEntityMapper.convertToGetProductDto(entity);
    }

    @Override
    public List<GetProductDto> getMany(List<Long> ids) {
        List<ProductEntity> productEntities = productRepository.findAllById(ids);
        return productEntities.stream().map(productEntityMapper::convertToGetProductDto).toList();
    }

    @Override
    public GetProductDto create(CreateOrUdpateProductDto dto) {
        ProductEntity productEntity = productEntityMapper.convertToEntity(dto);
        ProductEntity resultProductEntity = productRepository.save(productEntity);
        return productEntityMapper.convertToGetProductDto(resultProductEntity);
    }

    @Override
    public GetProductDto put(Long id, CreateOrUdpateProductDto dto) {
        ProductEntity productEntity = _getOne(id);

        productEntityMapper.updateWithNull(dto, productEntity);

        ProductEntity resultProductEntity = productRepository.save(productEntity);
        return productEntityMapper.convertToGetProductDto(resultProductEntity);
    }

    @Override
    public void delete(Long id) {
        ProductEntity productEntity = _getOne(id);
        if (productEntity != null) {
            productRepository.delete(productEntity);
        }
    }

    @Override
    public void deleteMany(List<Long> ids) {
        productRepository.deleteAllById(ids);
    }
}
