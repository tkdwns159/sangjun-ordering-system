package com.sangjun.order.domain.service.mapper;

import com.sangjun.common.domain.valueobject.*;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.entity.Product;
import com.sangjun.order.domain.entity.Restaurant;
import com.sangjun.order.domain.service.dto.create.CreateOrderCommand;
import com.sangjun.order.domain.service.dto.create.CreateOrderResponse;
import com.sangjun.order.domain.service.dto.create.OrderAddressDto;
import com.sangjun.order.domain.service.dto.create.OrderItemDto;
import com.sangjun.order.domain.service.dto.track.TrackOrderResponse;
import com.sangjun.order.domain.valueobject.StreetAddress;
import com.sangjun.order.domain.valueobject.TrackingId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.sangjun.order.domain.service.mapper.OrderMapstructMapper.MAPPER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OrderMapstructMapperTest {

    private static final UUID RESTAURANT_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();
    public static final UUID TRACKING_ID = UUID.randomUUID();
    private static final OrderAddressDto ORDER_ADDRESS = OrderAddressDto.builder()
            .city("Seoul")
            .postalCode("432")
            .street("Sillim")
            .build();

    private static Order ORDER;

    @BeforeAll
    static void setUp() {
        int quantity = 2;
        BigDecimal price = new BigDecimal("1000");
        BigDecimal subTotal = price.multiply(BigDecimal.valueOf(quantity));

        List<com.sangjun.order.domain.entity.OrderItem> items = getOrderItems(quantity, price, subTotal);

        ORDER = Order.builder()
                .restaurantId(new RestaurantId(RESTAURANT_ID))
                .customerId(new CustomerId(CUSTOMER_ID))
                .deliveryAddress(MAPPER.toStreetAddress(ORDER_ADDRESS))
                .items(items)
                .price(new Money(BigDecimal.valueOf(2000)))
                .orderStatus(OrderStatus.PENDING)
                .trackingId(new TrackingId(TRACKING_ID))
                .build();
    }

    private static List<com.sangjun.order.domain.entity.OrderItem> getOrderItems(int quantity, BigDecimal price, BigDecimal subTotal) {
        List<com.sangjun.order.domain.entity.OrderItem> items = new ArrayList<>();
        items.add(getOrderItem(quantity, price, subTotal));
        items.add(getOrderItem(quantity, price, subTotal));
        return items;
    }

    private static com.sangjun.order.domain.entity.OrderItem getOrderItem(int quantity, BigDecimal price, BigDecimal subTotal) {
        return com.sangjun.order.domain.entity.OrderItem.builder()
                .subTotal(new Money(subTotal))
                .quantity(quantity)
                .price(new Money(price))
                .product(Product.builder()
                        .id(new ProductId(UUID.randomUUID()))
                        .build())
                .build();
    }


    @Test
    void testCreateOrderCommandToRestaurant() {
        CreateOrderCommand command = CreateOrderCommand.builder()
                .restaurantId(RESTAURANT_ID)
                .build();

        Restaurant restaurant = MAPPER.toRestaurant(command);

        assertEquals(RESTAURANT_ID, restaurant.getId().getValue());
    }


    @Test
    void testOrderItemToProduct() {
        BigDecimal price = new BigDecimal("1000.00");

        OrderItemDto orderItemDto = OrderItemDto.builder()
                .productId(PRODUCT_ID)
                .price(price)
                .build();

        Product product = MAPPER.toProduct(orderItemDto);

        assertEquals(PRODUCT_ID, product.getId().getValue());
        assertEquals(price, product.getPrice().getAmount());
    }

    @Test
    void testOrderItemToOrderItemEntity() {
        BigDecimal price = new BigDecimal("1000.00");
        int quantity = 2;
        BigDecimal subTotal = new BigDecimal("2000.00");

        OrderItemDto orderItemDto = OrderItemDto.builder()
                .productId(PRODUCT_ID)
                .quantity(quantity)
                .price(price)
                .subTotal(subTotal)
                .build();

        com.sangjun.order.domain.entity.OrderItem orderItemEntity = MAPPER.toOrderItem(orderItemDto);
        assertEquals(PRODUCT_ID, orderItemEntity.getProduct().getId().getValue());
        assertEquals(quantity, orderItemEntity.getQuantity());
        assertEquals(subTotal, orderItemEntity.getSubTotal().getAmount());
        assertEquals(price, orderItemEntity.getPrice().getAmount());
    }

    @Test
    void testOrderAddressToStreetAddress() {
        StreetAddress streetAddress = MAPPER.toStreetAddress(ORDER_ADDRESS);
        assertNotNull(streetAddress.getId());
        assertEquals(ORDER_ADDRESS.getCity(), streetAddress.getCity());
        assertEquals(ORDER_ADDRESS.getStreet(), streetAddress.getStreet());
        assertEquals(ORDER_ADDRESS.getPostalCode(), streetAddress.getPostalCode());
    }

    @Test
    void testCreateOrderCommandToOrder() {
        BigDecimal price = new BigDecimal("1000.00");
        int quantity = 2;
        BigDecimal subTotal = price.multiply(BigDecimal.valueOf(quantity));

        List<OrderItemDto> items = new ArrayList<>();
        items.add(OrderItemDto.builder()
                .productId(PRODUCT_ID)
                .price(price)
                .quantity(quantity)
                .subTotal(subTotal)
                .build());

        CreateOrderCommand command = CreateOrderCommand.builder()
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .orderAddressDto(ORDER_ADDRESS)
                .items(items)
                .price(price)
                .build();

        Order order = MAPPER.toOrder(command);

        assertEquals(RESTAURANT_ID, order.getRestaurantId().getValue());
        assertEquals(CUSTOMER_ID, order.getCustomerId().getValue());
        assertEquals(price, order.getPrice().getAmount());
        assertNotNull(order.getItems());
        assertEquals(1, order.getItems().size());
        assertNotNull(order.getDeliveryAddress());
    }

    @Test
    void testOrderToRestaurant() {
        Restaurant restaurant = MAPPER.toRestaurant(ORDER);

        assertEquals(RESTAURANT_ID, restaurant.getId().getValue());

        for (int i = 0; i < restaurant.getProducts().size(); i++) {
            Product srcProduct = ORDER.getItems().get(i).getProduct();
            Product targetProduct = restaurant.getProducts().get(i);

            assertEquals(srcProduct.getId().getValue(), targetProduct.getId().getValue());
        }
    }

    @Test
    void testOrderToCreateOrderResponse() {
        CreateOrderResponse response = MAPPER.toCreateOrderResponse(ORDER);

        assertEquals(OrderStatus.PENDING, response.getOrderStatus());
        assertEquals(TRACKING_ID, response.getOrderTrackingId());
    }

    @Test
    void testOrderToTrackOrderResponse() {
        TrackOrderResponse response = MAPPER.toTrackOrderResponse(ORDER);

        assertEquals(ORDER.getTrackingId().getValue(), response.getOrderTrackingId());
        assertEquals(ORDER.getOrderStatus(), response.getOrderStatus());
    }
}
