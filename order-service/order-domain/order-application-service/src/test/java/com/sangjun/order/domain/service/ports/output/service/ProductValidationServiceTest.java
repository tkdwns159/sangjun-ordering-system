package com.sangjun.order.domain.service.ports.output.service;

import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.ProductId;
import com.sangjun.common.domain.valueobject.RestaurantId;
import com.sangjun.order.domain.service.ports.output.repository.RestaurantRepository;
import com.sangjun.order.domain.service.ports.output.service.product.ProductValidationService;
import com.sangjun.order.domain.valueobject.Product;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = ProductValidationTestConfig.class)
class ProductValidationServiceTest {

    @Autowired
    private ProductValidationService productValidationService;

    @Autowired
    private RestaurantRepository restaurantRepository;

    RestaurantId restaurantId = new RestaurantId(UUID.randomUUID());
    ProductId productId1 = new ProductId(UUID.randomUUID());
    ProductId productId2 = new ProductId(UUID.randomUUID());
    Money price1 = Money.of("3000");
    Money price2 = Money.of("4000");
    int quantity1 = 3;
    int quantity2 = 9;
    Product product1 = Product.builder()
            .id(productId1)
            .price(price1)
            .quantity(quantity1)
            .build();
    Product product2 = Product.builder()
            .id(productId2)
            .price(price2)
            .quantity(quantity2)
            .build();

    @Test
    void 요청한_제품id를_찾을수없으면_예외발생() {
        //given
        //when
        when(restaurantRepository.findProductsByRestaurantIdInProductIds(any(), anyList()))
                .thenReturn(List.of(
                        Product.builder()
                                .id(productId1)
                                .price(price1)
                                .quantity(quantity1 + 10)
                                .build()));

        //then
        Assertions.assertThatThrownBy(() ->
                productValidationService.validateProducts(restaurantId, List.of(product1, product2)));
    }

    @Test
    void 요청수량보다_재고수량이_적으면_예외발생() {
        //given
        //when
        when(restaurantRepository.findProductsByRestaurantIdInProductIds(any(), anyList()))
                .thenReturn(List.of(Product.builder()
                        .id(productId1)
                        .quantity(quantity1 - 1)
                        .price(price1)
                        .build()));
        //then
        Assertions.assertThatThrownBy(() ->
                productValidationService.validateProducts(restaurantId, List.of(product1)));
    }

    @Test
    void 제품가격정보가_불일치하는경우_예외발생() {
        //given

        //when
        when(restaurantRepository.findProductsByRestaurantIdInProductIds(any(), anyList()))
                .thenReturn(List.of(Product.builder()
                        .id(productId1)
                        .quantity(quantity1)
                        .price(price1.subtract(Money.of("100")))
                        .build()));

        //then
        Assertions.assertThatThrownBy(() ->
                productValidationService.validateProducts(restaurantId, List.of(product1)));
    }
}