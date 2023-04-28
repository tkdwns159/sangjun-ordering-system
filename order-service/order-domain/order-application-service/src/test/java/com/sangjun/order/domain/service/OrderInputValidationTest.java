package com.sangjun.order.domain.service;

import com.sangjun.order.domain.service.dto.create.CreateOrderCommand;
import com.sangjun.order.domain.service.dto.create.OrderAddress;
import com.sangjun.order.domain.service.dto.create.OrderItem;
import com.sangjun.order.domain.service.dto.track.TrackOrderQuery;
import com.sangjun.order.domain.service.ports.input.service.OrderApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = OrderTestConfiguration.class)
public class OrderInputValidationTest {

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID RESTAURANT_ID = UUID.randomUUID();
    private static final OrderAddress ORDER_ADDRESS = OrderAddress.builder()
            .city("Seoul")
            .postalCode("432")
            .street("Sillim")
            .build();

    @Autowired
    public OrderApplicationService orderApplicationService;

    @Test
    void 주문시_고객번호가_NULL이면_예외가_발생한다() {
        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .restaurantId(RESTAURANT_ID)
                .price(BigDecimal.ZERO)
                .orderAddress(ORDER_ADDRESS)
                .items(new ArrayList<>())
                .build();

        assertThrows(ConstraintViolationException.class,
                () -> orderApplicationService.createOrder(createOrderCommand));
    }

    @Test
    void 주문시_식당번호가_NULL이면_예외가_발생한다() {
        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .price(BigDecimal.ZERO)
                .orderAddress(ORDER_ADDRESS)
                .items(new ArrayList<>())
                .build();

        assertThrows(ConstraintViolationException.class,
                () -> orderApplicationService.createOrder(createOrderCommand));
    }

    @Test
    void 주문시_가격이_NULL이면_예외가_발생한다() {
        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .orderAddress(ORDER_ADDRESS)
                .items(new ArrayList<>())
                .build();

        assertThrows(ConstraintViolationException.class,
                () -> orderApplicationService.createOrder(createOrderCommand));
    }

    @Test
    void 주문시_주소가_NULL이면_예외가_발생한다() {
        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .price(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        assertThrows(ConstraintViolationException.class,
                () -> orderApplicationService.createOrder(createOrderCommand));
    }

    @Test
    void 주문시_주문물품이_NULL이면_예외가_발생한다() {
        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .price(BigDecimal.ZERO)
                .build();

        assertThrows(ConstraintViolationException.class,
                () -> orderApplicationService.createOrder(createOrderCommand));
    }

    @Test
    void 주문시_주문물품의_개수가_0개_이면_예외가_발생한다() {
        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .price(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        assertThrows(ConstraintViolationException.class,
                () -> orderApplicationService.createOrder(createOrderCommand));
    }

    @Test
    void 주문시_주문물품의_상품번호가_NULL이면_예외가_발생한다() {
        OrderItem orderItem = OrderItem.builder()
                .quantity(2)
                .price(new BigDecimal(1000))
                .subTotal(new BigDecimal(1000))
                .build();
        List<OrderItem> items = new ArrayList<>();
        items.add(orderItem);

        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .price(BigDecimal.ZERO)
                .items(items)
                .build();

        assertThrows(ConstraintViolationException.class,
                () -> orderApplicationService.createOrder(createOrderCommand));
    }

    @Test
    void 주문시_주문물품의_수량이_NULL이면_예외가_발생한다() {
        OrderItem orderItem = OrderItem.builder()
                .productId(UUID.randomUUID())
                .price(new BigDecimal(1000))
                .subTotal(new BigDecimal(1000))
                .build();
        List<OrderItem> items = new ArrayList<>();
        items.add(orderItem);

        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .price(BigDecimal.ZERO)
                .items(items)
                .build();

        assertThrows(ConstraintViolationException.class,
                () -> orderApplicationService.createOrder(createOrderCommand));
    }

    @Test
    void 주문시_주문물품의_가격이_NULL이면_예외가_발생한다() {
        OrderItem orderItem = OrderItem.builder()
                .productId(UUID.randomUUID())
                .quantity(2)
                .subTotal(new BigDecimal(1000))
                .build();
        List<OrderItem> items = new ArrayList<>();
        items.add(orderItem);

        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .price(BigDecimal.ZERO)
                .items(items)
                .build();

        assertThrows(ConstraintViolationException.class,
                () -> orderApplicationService.createOrder(createOrderCommand));
    }

    @Test
    void 주문시_주문물품의_subtotal이_NULL이면_예외가_발생한다() {
        OrderItem orderItem = OrderItem.builder()
                .productId(UUID.randomUUID())
                .quantity(2)
                .price(new BigDecimal(1000))
                .build();
        List<OrderItem> items = new ArrayList<>();
        items.add(orderItem);

        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .price(BigDecimal.ZERO)
                .items(items)
                .build();

        assertThrows(ConstraintViolationException.class,
                () -> orderApplicationService.createOrder(createOrderCommand));
    }

    @Test
    void 주문추적시_추적번호가_NULL이면_예외가_발생한다() {
        TrackOrderQuery query = TrackOrderQuery.builder().build();

        assertThrows(ConstraintViolationException.class,
                () -> orderApplicationService.trackOrder(query));
    }


}
