package com.sangjun.order.domain.service.ports.input.service;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.ProductId;
import com.sangjun.common.domain.valueobject.RestaurantId;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.service.dto.create.OrderAddressDto;
import com.sangjun.order.domain.service.dto.track.TrackOrderQuery;
import com.sangjun.order.domain.service.dto.track.TrackOrderResponse;
import com.sangjun.order.domain.service.ports.output.repository.CustomerRepository;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import com.sangjun.order.domain.valueobject.OrderItem;
import com.sangjun.order.domain.valueobject.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.sangjun.order.domain.service.mapper.OrderMapstructMapper.MAPPER;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = CreateOrderTestConfig.class)
public class TrackOrderTest {
    private static final UUID CUSTOMER_ID = UUID.fromString("f6316e90-1837-4940-b5db-a3c49a9a10ca");
    private static final UUID RESTAURANT_ID = UUID.fromString("ad68afcc-e55e-4e6a-bc6d-95a26a5410ff");
    private static final UUID PRODUCT_ID_1 = UUID.fromString("cb48e255-cc1c-4fc3-b80c-c4d73ca187dd");
    private static final UUID PRODUCT_ID_2 = UUID.fromString("d9e55ab9-68dc-4af5-b66f-a875b2df95fd");
    private static final Product PRODUCT_1 = Product.builder()
            .id(new ProductId(PRODUCT_ID_1))
            .price(Money.of(new BigDecimal("1000")))
            .quantity(100)
            .build();
    private static final Product PRODUCT_2 = Product.builder()
            .id(new ProductId(PRODUCT_ID_2))
            .price(Money.of(new BigDecimal("3200")))
            .quantity(100)
            .build();
    private static final OrderItem ORDER_ITEM_1 = OrderItem.builder()
            .price(PRODUCT_1.getPrice())
            .quantity(2)
            .subTotal(PRODUCT_1.getPrice().multiply(2))
            .productId(PRODUCT_1.getId())
            .build();
    private static final OrderItem ORDER_ITEM_2 = OrderItem.builder()
            .price(PRODUCT_2.getPrice())
            .quantity(1)
            .subTotal(PRODUCT_2.getPrice())
            .productId(PRODUCT_2.getId())
            .build();
    private static final OrderAddressDto ORDER_ADDRESS = OrderAddressDto.builder()
            .street("Sillim")
            .city("Seoul")
            .postalCode("4321")
            .build();
    private static final Order ORDER = Order.builder()
            .customerId(new CustomerId(CUSTOMER_ID))
            .restaurantId(new RestaurantId(RESTAURANT_ID))
            .deliveryAddress(MAPPER.toStreetAddress(ORDER_ADDRESS))
            .failureMessages(new ArrayList<>())
            .items(List.of(ORDER_ITEM_1, ORDER_ITEM_2))
            .price(ORDER_ITEM_1.getSubTotal().add(ORDER_ITEM_2.getSubTotal()))
            .build();

    @BeforeEach
    void initOrder() {
        ORDER.initialize();
    }

    @Autowired
    private TrackOrderApplicationService trackOrderService;
    @Autowired
    private OrderRepository orderRepository;
    @MockBean
    private CustomerRepository customerRepository;

    @Test
    void 주문_조회() {
        //given
        orderRepository.save(ORDER);
        TrackOrderQuery trackOrderQuery = TrackOrderQuery.builder()
                .orderTrackingId(ORDER.getTrackingId().getValue())
                .build();

        //when
        TrackOrderResponse response = trackOrderService.trackOrder(trackOrderQuery);

        //then
        assertThat(response.getOrderTrackingId())
                .isEqualTo(ORDER.getTrackingId().getValue());
        assertThat(response.getOrderStatus())
                .isEqualTo(ORDER.getOrderStatus());
    }

}
