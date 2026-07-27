package com.example.service;

import com.example.common_lib.config.ServiceUrlsProperties;
import com.example.common_lib.utils.exception.CommonConflictException;
import com.example.controller.dto.cart.CreateCartDto;
import com.example.controller.dto.cart.GetCartDto;
import com.example.controller.dto.cart_item.CreateCartItemDto;
import com.example.controller.dto.cart_item.UpdateCartItemDto;
import com.example.repository.cart.CartRepository;
import com.example.repository.cart.entity.CartEntity;
import com.example.repository.cart_item.CartItemRepository;
import com.example.repository.cart_item.entity.CartItemEntity;
import com.example.utils.CartEntityMapper;
import com.example.utils.CartItemEntityMapper;
import com.example.common_lib.utils.exception.CommonEntityNotFoundException;
import com.example.common_lib.dto.GetAccountDto;
import com.example.common_lib.dto.GetProductDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CartServiceImpl implements CartService {
    private final WebClient webClient;

    private final ServiceUrlsProperties serviceUrls;

    private final CartEntityMapper cartEntityMapper;

    private final CartItemEntityMapper cartItemEntityMapper;

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private CartEntity getRequiredCart(Long id) {
        return cartRepository.findById(id).orElseThrow(() -> new CommonEntityNotFoundException("Cart with id `%s` not found".formatted(id)));
    }

    public GetAccountDto getRequiredAccount(Long id) {
        return webClient
                .get()
                .uri(serviceUrls.getAccount() + "/rest/accounts/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new CommonEntityNotFoundException("Account with id `%s` not found".formatted(id))))
                .bodyToMono(GetAccountDto.class)
                .block();
    }

    public GetProductDto getRequiredProduct(Long id) {
        return webClient
                .get()
                .uri(serviceUrls.getProducts() + "/rest/products/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new CommonEntityNotFoundException("Product with id `%s` not found".formatted(id))))
                .bodyToMono(GetProductDto.class)
                .block();
    }

    private CartItemEntity getRequiredCartItem(Long id) {
        return cartItemRepository.findById(id).orElseThrow(() -> new CommonEntityNotFoundException("Cart item with id `%s` not found".formatted(id)));
    }

    @Override
    public Page<GetCartDto> getAll(Pageable pageable) {
        Page<CartEntity> cartEntities = cartRepository.findAll(pageable);
        return cartEntities.map(cartEntityMapper::convertToGetCartDto);
    }

    @Override
    public GetCartDto getOne(Long id) {
        CartEntity entity = getRequiredCart(id);
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
    public GetCartDto create(CreateCartDto dto) {
        GetAccountDto account = getRequiredAccount(dto.account_id());

        CartEntity cartEntity = new CartEntity();
        cartEntity.setAccount_id(account.id());

        CartEntity resultCartEntity = cartRepository.save(cartEntity);
        return cartEntityMapper.convertToGetCartDto(resultCartEntity);
    }

    @Transactional
    @Override
    public void addToCart(CreateCartItemDto dto) {
        /**
         * Ищем имеется ли такой продукт
         */
        GetProductDto productEntity = getRequiredProduct(dto.product_id());
        /**
         * Проверяем есть ли такой продукт уже в корзине
         */
        CartEntity cartEntity = getRequiredCart(dto.cart_id());
        /**
         * Ищем продукт в корзине
         */
        Optional<CartItemEntity> existingItem = cartEntity.getProducts().stream()
            .filter(item -> item.getProductId().equals(productEntity.id()))
            .findFirst();

        if (existingItem.isPresent()) {
            CartItemEntity cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + dto.quantity());
            cartItemRepository.save(cartItem);
        } else {
            CartItemEntity cartItem = new CartItemEntity();
            cartItem.setCart(cartEntity);
            cartItem.setProductId(productEntity.id());
            cartItem.setQuantity(dto.quantity());
            cartItemRepository.save(cartItem);
        }
    }

    @Transactional
    @Override
    public void changeQuantity(UpdateCartItemDto dto) {
        /**
         * Проверяем есть ли такой продукт уже в корзине
         */
        CartItemEntity cartItemEntity = getRequiredCartItem(dto.id());
        /**
         * Ищем имеется ли такой продукт
         */
        Long productId = cartItemEntity.getProductId();
        GetProductDto productEntity = getRequiredProduct(productId);
        Integer zeroQuantity = 0;
        /**
         * Проверка на случай когда прилетит quantity == 0
         */
        if (dto.quantity().equals(zeroQuantity)) {
            cartItemRepository.delete(cartItemEntity);
        } else {
            if (!productEntity.isActive()) throw new CommonConflictException("Product with id `%s` must be active".formatted(productId));
            CartItemEntity mappedEntity = cartItemEntityMapper.updateWithNull(dto, cartItemEntity);
            cartItemRepository.save(mappedEntity);
        }
    }

    @Override
    public void removeFromCart(Long id) {
        CartItemEntity entity = getRequiredCartItem(id);
        cartItemRepository.delete(entity);
    }

    @Override
    public void removeManyFromCart(List<Long> ids) {
        cartItemRepository.deleteAllById(ids);
    }
}
