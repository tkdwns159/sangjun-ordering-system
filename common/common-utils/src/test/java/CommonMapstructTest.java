import com.sangjun.common.domain.valueobject.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static com.sangjun.common.domain.mapper.CommonMapstructMapper.MAPPER;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommonMapstructTest {

    private static final UUID RESTAURANT_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();
    public static final UUID ORDER_ID = UUID.randomUUID();

    @Test
    void testBigDecimalToMoney() {
        BigDecimal price = new BigDecimal("1000.00");
        Money money = MAPPER.toMoney(price);

        assertEquals(price, money.getAmount());
    }

    @Test
    void testMoneyToBigDecimal() {
        Money money = new Money(new BigDecimal("1000.00"));
        BigDecimal price = MAPPER.toBigDecimal(money);

        assertEquals(price, money.getAmount());
    }

    @Test
    void testUUID_ToEntityId() {
        CustomerId customerId = MAPPER.toCustomerId(CUSTOMER_ID);
        ProductId productId = MAPPER.toProductId(PRODUCT_ID);
        RestaurantId restaurantId = MAPPER.toRestaurantId(RESTAURANT_ID);
        OrderId orderId = MAPPER.toOrderId(ORDER_ID);


        assertEquals(CUSTOMER_ID, customerId.getValue());
        assertEquals(PRODUCT_ID, productId.getValue());
        assertEquals(RESTAURANT_ID, restaurantId.getValue());
        assertEquals(ORDER_ID, orderId.getValue());
    }

    @Test
    void testEntityId_ToUUID() {
        CustomerId customerId = new CustomerId(CUSTOMER_ID);
        RestaurantId restaurantId = new RestaurantId(RESTAURANT_ID);
        OrderId orderId = new OrderId(ORDER_ID);

        assertEquals(CUSTOMER_ID, MAPPER.toUUID(customerId));
        assertEquals(RESTAURANT_ID, MAPPER.toUUID(restaurantId));
        assertEquals(ORDER_ID, MAPPER.toUUID(orderId));
    }
}
