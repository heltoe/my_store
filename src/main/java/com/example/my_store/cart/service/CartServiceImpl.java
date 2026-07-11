package com.example.my_store.cart.service;

import com.example.my_store.cart.controller.dto.cart.CreateCartDto;
import com.example.my_store.cart.controller.dto.cart.GetCartDto;
import com.example.my_store.cart.controller.dto.cart_item.CreateCartItemDto;
import com.example.my_store.cart.controller.dto.cart_item.UpdateCartItemDto;
import com.example.my_store.cart.repository.cart.entity.CartEntity;
import com.example.my_store.cart.repository.cart.CartRepository;
import com.example.my_store.cart.repository.cart_item.CartItemRepository;
import com.example.my_store.cart.repository.cart_item.entity.CartItemEntity;
import com.example.my_store.cart.utils.CartEntityMapper;
import com.example.my_store.cart.utils.CartItemEntityMapper;
import com.example.my_store.account.repository.AccountRepository;
import com.example.my_store.account.repository.entity.AccountEntity;
import com.example.my_store.product.repository.ProductRepository;
import com.example.my_store.product.repository.entity.ProductEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.my_store.utils.exception.CommonEntityNotFoundException;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CartServiceImpl implements CartService {

    private final CartEntityMapper cartEntityMapper;

    private final CartItemEntityMapper cartItemEntityMapper;

    private final CartRepository cartRepository;

    private final AccountRepository accountRepository;

    private final ProductRepository productRepository;

    private final CartItemRepository cartItemRepository;

    private CartEntity getRequiredCart(Long id) {
        return cartRepository.findById(id).orElseThrow(() -> new CommonEntityNotFoundException("Cart with id `%s` not found".formatted(id)));
    }

    private AccountEntity getRequiredAccount(Long id) {
        return accountRepository.findById(id).orElseThrow(() -> new CommonEntityNotFoundException("Account with id `%s` not found".formatted(id)));
    }

    private CartItemEntity getRequiredCartItem(Long id) {
        return cartItemRepository.findById(id).orElseThrow(() -> new CommonEntityNotFoundException("Cart item with id `%s` not found".formatted(id)));
    }

    private ProductEntity getRequiredProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new CommonEntityNotFoundException("Product with id `%s` not found".formatted(id)));
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
        AccountEntity account = getRequiredAccount(dto.account_id());

        CartEntity cartEntity = new CartEntity();
        cartEntity.setAccount(account);

        CartEntity resultCartEntity = cartRepository.save(cartEntity);
        return cartEntityMapper.convertToGetCartDto(resultCartEntity);
    }

    @Transactional
    @Override
    public void addToCart(CreateCartItemDto dto) {
        /**
         * Ищем имеется ли такой продукт
         */
        ProductEntity productEntity = getRequiredProduct(dto.product_id());
        /**
         * Проверяем есть ли такой продукт уже в корзине
         */
        CartEntity cartEntity = getRequiredCart(dto.cart_id());
        /**
         * Ищем продукт в корзине
         */
        Optional<CartItemEntity> existingItem = cartEntity.getProducts().stream()
            .filter(item -> item.getProduct().getId().equals(productEntity.getId()))
            .findFirst();

        if (existingItem.isPresent()) {
            CartItemEntity cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + dto.quantity());
            cartItemRepository.save(cartItem);
        } else {
            CartItemEntity cartItem = new CartItemEntity();
            cartItem.setCart(cartEntity);
            cartItem.setProduct(productEntity);
            cartItem.setQuantity(dto.quantity());
            cartItemRepository.save(cartItem);
        }
    }

    @Transactional
    @Override
    public void changeQuantity(UpdateCartItemDto dto) {
        CartItemEntity cartItemEntity = getRequiredCartItem(dto.id());
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
        CartItemEntity entity = getRequiredCartItem(id);
        cartItemRepository.delete(entity);
    }

    @Override
    public void removeManyFromCart(List<Long> ids) {
        cartItemRepository.deleteAllById(ids);
    }
}
