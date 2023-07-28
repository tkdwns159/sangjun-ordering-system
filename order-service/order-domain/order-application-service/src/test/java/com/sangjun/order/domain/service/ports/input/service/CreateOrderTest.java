package com.sangjun.order.domain.service.ports.input.service;

import com.sangjun.common.domain.valueobject.OrderStatus;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.service.dto.create.CreateOrderCommand;
import com.sangjun.order.domain.service.dto.create.CreateOrderResponse;
import com.sangjun.order.domain.service.dto.create.OrderAddressDto;
import com.sangjun.order.domain.service.dto.create.OrderItemDto;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import com.sangjun.order.domain.valueobject.OrderItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = CreateOrderTestConfig.class)
class CreateOrderTest {

    @Autowired
    private CreateOrderApplicationService createOrderService;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void 주문정보_저장() {
        // given
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        BigDecimal totalPrice = new BigDecimal("13800");
        OrderItemDto orderItemDto1 = OrderItemDto.builder()
                .price(new BigDecimal("3000"))
                .subTotal(new BigDecimal("9000"))
                .productId(productId1)
                .quantity(3)
                .build();
        OrderItemDto orderItemDto2 = OrderItemDto.builder()
                .price(new BigDecimal("2400"))
                .subTotal(new BigDecimal("4800"))
                .productId(productId2)
                .quantity(2)
                .build();
        List<OrderItemDto> items = new ArrayList<>(Arrays.asList(orderItemDto1, orderItemDto2));
        OrderAddressDto orderAddressDto = OrderAddressDto.builder()
                .city("Seoul")
                .postalCode("12345")
                .street("Sillim")
                .build();

        CreateOrderCommand command = CreateOrderCommand.builder()
                .items(items)
                .price(totalPrice)
                .restaurantId(restaurantId)
                .customerId(customerId)
                .orderAddressDto(orderAddressDto)
                .build();

        // when
        CreateOrderResponse response = createOrderService.createOrder(command);

        // then
        UUID orderTrackingId = response.getOrderTrackingId();
        Order foundOrder = orderRepository.findByTrackingId(orderTrackingId).get();

        assertThat(foundOrder.getTrackingId().getValue())
                .isEqualTo(orderTrackingId);
        assertThat(foundOrder.getOrderStatus())
                .isEqualTo(OrderStatus.PENDING);
        assertThat(foundOrder.getRestaurantId().getValue())
                .isEqualTo(restaurantId);
        checkDeliveryAddress(orderAddressDto, foundOrder);
        assertThat(foundOrder.getCustomerId().getValue())
                .isEqualTo(customerId);
        assertThat(foundOrder.getPrice().getAmount())
                .isEqualTo(totalPrice);
        checkOrderItem(foundOrder, 0, orderItemDto1);
        checkOrderItem(foundOrder, 1, orderItemDto2);
    }

    private void checkDeliveryAddress(OrderAddressDto orderAddressDto, Order foundOrder) {
        assertThat(foundOrder.getDeliveryAddress().getCity())
                .isEqualTo(orderAddressDto.getCity());
        assertThat(foundOrder.getDeliveryAddress().getStreet())
                .isEqualTo(orderAddressDto.getStreet());
        assertThat(foundOrder.getDeliveryAddress().getPostalCode())
                .isEqualTo(orderAddressDto.getPostalCode());
    }

    private void checkOrderItem(Order order,
                                Integer itemNumber,
                                OrderItemDto orderItemDto) {
        final OrderItem orderItem = order.getItems().get(itemNumber);

        assertThat(orderItem.getOrderItemId().getOrderId())
                .isEqualTo(order.getId());
        assertThat(orderItem.getOrderItemId().getOrderItemId())
                .isEqualTo(itemNumber + 1);
        assertThat(orderItem.getProductId().getValue())
                .isEqualTo(orderItemDto.getProductId());
        assertThat(orderItem.getQuantity())
                .isEqualTo(orderItemDto.getQuantity());
        assertThat(orderItem.getPrice().getAmount())
                .isEqualTo(orderItemDto.getPrice());
        assertThat(orderItem.getSubTotal().getAmount())
                .isEqualTo(orderItemDto.getSubTotal());
    }


}