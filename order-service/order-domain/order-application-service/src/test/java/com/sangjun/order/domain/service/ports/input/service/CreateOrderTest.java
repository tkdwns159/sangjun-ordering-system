package com.sangjun.order.domain.service.ports.input.service;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.OrderStatus;
import com.sangjun.common.domain.valueobject.ProductId;
import com.sangjun.order.domain.entity.Customer;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.service.dto.create.CreateOrderCommand;
import com.sangjun.order.domain.service.dto.create.CreateOrderResponse;
import com.sangjun.order.domain.service.dto.create.OrderAddressDto;
import com.sangjun.order.domain.service.dto.create.OrderItemDto;
import com.sangjun.order.domain.service.ports.output.repository.CustomerRepository;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import com.sangjun.order.domain.service.ports.output.repository.RestaurantRepository;
import com.sangjun.order.domain.valueobject.OrderItem;
import com.sangjun.order.domain.valueobject.Product;
import com.sangjun.order.domain.valueobject.TrackingId;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@SpringBootTest(classes = CreateOrderTestConfig.class)
@Slf4j
class CreateOrderTest {

    @Autowired
    private CreateOrderApplicationService createOrderService;

    @Autowired
    private OrderRepository orderRepository;

    UUID productId1 = UUID.randomUUID();
    UUID productId2 = UUID.randomUUID();
    UUID restaurantId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();
    Money price1 = Money.of("3000");
    Money price2 = Money.of("2400");

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private RestaurantRepository restaurantRepository;


    @BeforeEach
    void preCondition() {
        Customer customer = new Customer(new CustomerId(customerId));

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));
        when(restaurantRepository.findProductsByRestaurantIdInProductIds(any(), anyList()))
                .thenReturn(List.of(
                        Product.builder()
                                .id(new ProductId(productId1))
                                .price(price1)
                                .quantity(100)
                                .build(),
                        Product.builder()
                                .id(new ProductId(productId2))
                                .price(price2)
                                .quantity(100)
                                .build()
                ));
    }

    @Test
    void 주문정보_저장() {
        // given
        Money totalPrice = Money.of("13800");
        OrderItemDto orderItemDto1 = OrderItemDto.builder()
                .price(price1.getAmount())
                .subTotal(price1.multiply(3).getAmount())
                .productId(productId1)
                .quantity(3)
                .build();
        OrderItemDto orderItemDto2 = OrderItemDto.builder()
                .price(price2.getAmount())
                .subTotal(price2.multiply(2).getAmount())
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
                .price(totalPrice.getAmount())
                .restaurantId(restaurantId)
                .customerId(customerId)
                .orderAddressDto(orderAddressDto)
                .build();

        // when
        CreateOrderResponse response = createOrderService.createOrder(command);

        // then
        TrackingId orderTrackingId = new TrackingId(response.getOrderTrackingId());
        Order foundOrder = orderRepository.findByTrackingId(orderTrackingId).get();
        checkOrder(restaurantId, customerId, totalPrice, orderItemDto1, orderItemDto2, orderAddressDto,
                orderTrackingId, foundOrder);
    }

    private void checkOrder(UUID restaurantId, UUID customerId, Money totalPrice, OrderItemDto orderItemDto1,
                            OrderItemDto orderItemDto2, OrderAddressDto orderAddressDto, TrackingId orderTrackingId, Order foundOrder) {
        assertThat(foundOrder.getTrackingId())
                .isEqualTo(orderTrackingId);
        assertThat(foundOrder.getOrderStatus())
                .isEqualTo(OrderStatus.PENDING);
        assertThat(foundOrder.getRestaurantId().getValue())
                .isEqualTo(restaurantId);
        checkDeliveryAddress(orderAddressDto, foundOrder);
        assertThat(foundOrder.getCustomerId().getValue())
                .isEqualTo(customerId);
        assertThat(foundOrder.getPrice().getAmount())
                .isEqualTo(totalPrice.getAmount());
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
        final OrderItem orderItem = order.getItemOfIndex(itemNumber);

        assertThat(orderItem.getId().getOrderId())
                .isEqualTo(order.getId());
        assertThat(orderItem.getId().getOrderItemId())
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

    @Test
    void 개별주문항목의_합계가_단가X수량이_아니면_예외발생() {
        // given
        Money totalPrice = Money.of("13800");
        OrderItemDto orderItemDto1 = OrderItemDto.builder()
                .price(new BigDecimal("3000.00"))
                .subTotal(new BigDecimal("9000.00"))
                .productId(productId1)
                .quantity(3)
                .build();
        OrderItemDto orderItemDto2 = OrderItemDto.builder()
                .price(new BigDecimal("2400.00"))
                .subTotal(new BigDecimal("5000.00"))
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
                .price(totalPrice.getAmount())
                .restaurantId(restaurantId)
                .customerId(customerId)
                .orderAddressDto(orderAddressDto)
                .build();

        // when, then
        assertThatThrownBy(() -> createOrderService.createOrder(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("price * quantity")
                .hasMessageContaining("subTotal");
    }

    @Test
    void 개별주문항목의_상품_검증실패시_예외발생() {
        // given
        Money totalPrice = Money.of("13800");
        OrderItemDto orderItemDto1 = OrderItemDto.builder()
                .price(new BigDecimal("3000.00"))
                .subTotal(new BigDecimal("9000.00"))
                .productId(productId1)
                .quantity(3)
                .build();
        OrderItemDto orderItemDto2 = OrderItemDto.builder()
                .price(new BigDecimal("2400.00"))
                .subTotal(new BigDecimal("4800.00"))
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
                .price(totalPrice.getAmount())
                .restaurantId(restaurantId)
                .customerId(customerId)
                .orderAddressDto(orderAddressDto)
                .build();
        // when
        when(restaurantRepository.findProductsByRestaurantIdInProductIds(any(), anyList()))
                .thenReturn(List.of(
                        Product.builder()
                                .id(new ProductId(productId1))
                                .quantity(100)
                                .price(Money.of("4000"))
                                .build(),
                        Product.builder()
                                .id(new ProductId(productId2))
                                .quantity(100)
                                .price(Money.of("2400"))
                                .build()));

        // then
        assertThatThrownBy(() -> createOrderService.createOrder(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 주문에_명시된_가격과_주문항목_가격의합이_다르면_예외발생() {
        // given
        Money totalPrice = Money.of("13500");
        OrderItemDto orderItemDto1 = OrderItemDto.builder()
                .price(new BigDecimal("3000.00"))
                .subTotal(new BigDecimal("9000.00"))
                .productId(productId1)
                .quantity(3)
                .build();
        OrderItemDto orderItemDto2 = OrderItemDto.builder()
                .price(new BigDecimal("2400.00"))
                .subTotal(new BigDecimal("4800.00"))
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
                .price(totalPrice.getAmount())
                .restaurantId(restaurantId)
                .customerId(customerId)
                .orderAddressDto(orderAddressDto)
                .build();

        // when, then
        assertThatThrownBy(() -> createOrderService.createOrder(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("order price")
                .hasMessageContaining("the sum of order items price");
    }

    @Test
    void 등록된_고객이_아니면_예외발생() {
        // given
        Money totalPrice = Money.of("13800");
        OrderItemDto orderItemDto1 = OrderItemDto.builder()
                .price(new BigDecimal("3000.00"))
                .subTotal(new BigDecimal("9000.00"))
                .productId(productId1)
                .quantity(3)
                .build();
        OrderItemDto orderItemDto2 = OrderItemDto.builder()
                .price(new BigDecimal("2400.00"))
                .subTotal(new BigDecimal("4800.00"))
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
                .price(totalPrice.getAmount())
                .restaurantId(restaurantId)
                .customerId(customerId)
                .orderAddressDto(orderAddressDto)
                .build();

        // when
        when(customerRepository.findById(Mockito.any()))
                .thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> createOrderService.createOrder(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("customer")
                .hasMessageContaining("not found");
    }
}