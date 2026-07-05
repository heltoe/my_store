package com.example.my_store.cart.service;

import com.example.my_store.cart.controller.dto.cart.GetCartDto;
import com.example.my_store.cart.controller.dto.cart_item.CreateCartItemDto;
import com.example.my_store.cart.controller.dto.cart_item.UpdateCartItemDto;
import com.example.my_store.cart.repository.cart.entity.CartEntity;
import com.example.my_store.cart.repository.cart.CartRepository;
import com.example.my_store.cart.repository.cart_item.CartItemRepository;
import com.example.my_store.cart.repository.cart_item.entity.CartItemEntity;
import com.example.my_store.cart.utils.CartEntityMapper;
import com.example.my_store.cart.utils.CartItemEntityMapper;
import com.example.my_store.product.repository.ProductRepository;
import com.example.my_store.product.repository.entity.ProductEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CartServiceImpl implements CartService {

    private final CartEntityMapper cartEntityMapper;

    private final CartItemEntityMapper cartItemEntityMapper;

    private final CartRepository cartRepository;

    private final ProductRepository productRepository;

    private final CartItemRepository cartItemRepository;

    public CartEntity _getOneCart(Long id) {
        return cartRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart with id `%s` not found".formatted(id)));
    }

    private CartItemEntity _getOneCartItem(Long id) {
        return cartItemRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item with id `%s` not found".formatted(id)));
    }

    private ProductEntity _getOneProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with id `%s` not found".formatted(id)));
    }

    @Override
    public Page<GetCartDto> getAll(Pageable pageable) {
        Page<CartEntity> cartEntities = cartRepository.findAll(pageable);
        return cartEntities.map(cartEntityMapper::convertToGetCartDto);
    }

    @Override
    public GetCartDto getOne(Long id) {
        CartEntity entity = _getOneCart(id);
        return cartEntityMapper.convertToGetCartDto(entity);
    }

    @Override
    public List<GetCartDto> getMany(List<Long> ids) {
        List<CartEntity> cartEntities = cartRepository.findAllById(ids);
        return cartEntities.stream()
                .map(cartEntityMapper::convertToGetCartDto)
                .toList();
    }

    @Override
    public GetCartDto create(GetCartDto dto) {
        CartEntity cartEntity = cartEntityMapper.convertToEntity(dto);
        CartEntity resultCartEntity = cartRepository.save(cartEntity);
        return cartEntityMapper.convertToGetCartDto(resultCartEntity);
    }

    @Transactional
    @Override
    public void addToCart(CreateCartItemDto dto) {
        /**
         * Ищем имеется ли такой продукт
         */
        _getOneProduct(dto.product_id());
        /**
         * Проверяем есть ли такой продукт уже в корзине
         */
        CartEntity cartEntity = _getOneCart(dto.cart_id());
        Optional<CartItemEntity> findCartItemEntity = cartEntity.getProducts().stream().filter(item -> item.getProduct().getId().equals(dto.product_id())).findFirst();
        if (findCartItemEntity.isPresent()) {
            CartItemEntity cartItem = findCartItemEntity.get();
            cartItem.setQuantity(dto.quantity());
            cartEntity.getProducts().add(cartItem);
            cartItemRepository.save(cartItem);
        } else {
            CartItemEntity mappedEntity = cartItemEntityMapper.convertToEntity(dto);
            CartItemEntity resultEntity = cartItemRepository.save(mappedEntity);
            cartItemRepository.save(resultEntity);
        }
    }

    @Transactional
    @Override
    public void changeQuantity(UpdateCartItemDto dto) {
        CartItemEntity cartItemEntity = _getOneCartItem(dto.id());
        Integer zeroQuantity = 0;
        /**
         * Проверка на случай когда прилетит quantity == 0
         */
        if (dto.quantity().equals(zeroQuantity)) {
            cartItemRepository.delete(cartItemEntity);
        } else {
            CartItemEntity mappedEntity = cartItemEntityMapper.updateWithNull(dto, cartItemEntity);
            cartItemRepository.save(mappedEntity);
        }
    }

    @Override
    public void removeFromCart(Long id) {
        CartItemEntity entity = _getOneCartItem(id);
        if (entity != null) {
            cartItemRepository.delete(entity);
        }
    }

    @Override
    public void removeManyFromCart(List<Long> ids) {
        cartItemRepository.deleteAllById(ids);
    }
}
