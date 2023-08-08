package com.sangjun.restaurant.application.ports.input.message.listener;

import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.common.domain.valueobject.RestaurantOrderStatus;
import com.sangjun.restaurant.application.dto.ProductDto;
import com.sangjun.restaurant.application.dto.RestaurantApprovalRequest;
import com.sangjun.restaurant.application.ports.output.message.repository.PendingOrderRepository;
import com.sangjun.restaurant.application.ports.output.message.repository.RestaurantRepository;
import com.sangjun.restaurant.domain.entity.PendingOrder;
import com.sangjun.restaurant.domain.entity.Product;
import com.sangjun.restaurant.domain.entity.Restaurant;
import com.sangjun.restaurant.domain.valueobject.PendingOrderStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = TestConfig.class)
class RestaurantApprovalRequestMessageListenerTest {

    @Autowired
    private RestaurantApprovalRequestMessageListener listener;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private PendingOrderRepository pendingOrderRepository;

    private Restaurant restaurant;

    @BeforeAll
    void setup() {
        restaurant = 주문받을_식당과_제품들이_등록되어있음();
    }

    private Restaurant 주문받을_식당과_제품들이_등록되어있음() {
        var product1 = Product.builder()
                .name("product1")
                .price(Money.of("3300"))
                .available(true)
                .quantity(100)
                .build();
        var product2 = Product.builder()
                .name("product2")
                .price(Money.of("1230"))
                .available(true)
                .quantity(100)
                .build();

        Restaurant restaurant = Restaurant.builder()
                .isActive(true)
                .products(List.of(product1, product2))
                .build();
        return restaurantRepository.save(restaurant);
    }

    @Test
    void 상품가격_총합과_지불가격이_다르면_예외발생() {
        //given
        UUID orderId = UUID.randomUUID();
        var products = restaurant.getProducts();
        Money exactPrice = products.get(0).getPrice().multiply(2)
                .add(products.get(1).getPrice());
        Money price = exactPrice.subtract(Money.of("100"));

        var productDto1 = ProductDto.builder()
                .productId(products.get(0).getId())
                .quantity(2)
                .build();
        var productDto2 = ProductDto.builder()
                .productId(products.get(1).getId())
                .quantity(1)
                .build();

        //when, then
        var request = RestaurantApprovalRequest.builder()
                .restaurantId(restaurant.getId().getValue().toString())
                .orderId(orderId.toString())
                .price(price.getAmount())
                .createdAt(Instant.now())
                .products(List.of(productDto1, productDto2))
                .restaurantOrderStatus(RestaurantOrderStatus.PAID)
                .build();

        Assertions.assertThatThrownBy(() -> listener.registerPendingOrder(request))
                .hasMessageContaining("given price")
                .hasMessageContaining("actual total price");
    }


    @Test
    void 해당_식당에서_취급하지않는_상품이_있을경우_예외발생() {
        //given
        Restaurant restaurant2 = 주문받을_식당과_제품들이_등록되어있음();
        var products1 = restaurant.getProducts();
        var products2 = restaurant2.getProducts();
        var productDto1 = ProductDto.builder()
                .productId(products1.get(0).getId())
                .quantity(2)
                .build();
        var productDto2 = ProductDto.builder()
                .productId(products2.get(1).getId())
                .quantity(1)
                .build();
        Money price = products1.get(0).getPrice().multiply(2)
                .add(products2.get(1).getPrice());
        UUID orderId = UUID.randomUUID();

        //when, then
        var request = RestaurantApprovalRequest.builder()
                .restaurantId(restaurant.getId().getValue().toString())
                .orderId(orderId.toString())
                .price(price.getAmount())
                .createdAt(Instant.now())
                .products(List.of(productDto1, productDto2))
                .restaurantOrderStatus(RestaurantOrderStatus.PAID)
                .build();

        Assertions.assertThatThrownBy(() -> listener.registerPendingOrder(request))
                .hasMessageContaining("product")
                .hasMessageContaining("restaurant");
    }

    @Test
    void 주문대기처리() {
        //given
        UUID orderId = UUID.randomUUID();
        var products = restaurant.getProducts();
        Money price = products.get(0).getPrice().multiply(2)
                .add(products.get(1).getPrice());

        var productDto1 = ProductDto.builder()
                .productId(products.get(0).getId())
                .quantity(2)
                .build();
        var productDto2 = ProductDto.builder()
                .productId(products.get(1).getId())
                .quantity(1)
                .build();

        //when
        var request = RestaurantApprovalRequest.builder()
                .restaurantId(restaurant.getId().getValue().toString())
                .orderId(orderId.toString())
                .price(price.getAmount())
                .createdAt(Instant.now())
                .products(List.of(productDto1, productDto2))
                .restaurantOrderStatus(RestaurantOrderStatus.PAID)
                .build();
        listener.registerPendingOrder(request);

        //then
        PendingOrder pendingOrder = pendingOrderRepository.findByOrderId(new OrderId(orderId)).get();
        Assertions.assertThat(pendingOrder.getStatus())
                .isEqualTo(PendingOrderStatus.PENDING);
        Assertions.assertThat(pendingOrder.getRestaurantId())
                .isEqualTo(restaurant.getId());
    }

}
