package com.sangjun.order.dataaccess.customer;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.ProductId;
import com.sangjun.common.domain.valueobject.RestaurantId;
import com.sangjun.order.dataaccess.restaurant.ProductJpaRepository;
import com.sangjun.order.domain.entity.Customer;
import com.sangjun.order.domain.entity.Product;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@EntityScan(basePackages = "com.sangjun.order.domain")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2,
        replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class JdbcTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    void contextLoads() {
        jdbcTemplate.execute("create schema customer");
        jdbcTemplate.execute("create schema restaurant");
        jdbcTemplate.execute("create table customer.customers (id uuid primary key)");
        jdbcTemplate.execute("create table restaurant.product (id uuid primary key, restaurant_id uuid," +
                " quantity integer, price number)");
    }

    @Autowired
    private CustomerJpaRepository customerJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Test
    void testFindCustomer() {
        UUID customerId = UUID.randomUUID();
        insertCustomer(customerId);

        Optional<Customer> result = customerJpaRepository.findById(new CustomerId(customerId));

        Assertions.assertThat(result.isEmpty())
                .isFalse();
    }

    void insertCustomer(UUID uuid) {
        final String q = "insert into customer.customers (id) values(?)";
        jdbcTemplate.update(q, uuid);
    }

    @Test
    void testFindProduct() {
        UUID productId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        Product product = Product.builder()
                .id(new ProductId(productId))
                .quantity(10)
                .price(Money.of("1234"))
                .build();

        insertProduct(product, restaurantId);

        List<Product> result = productJpaRepository
                .findProductsByRestaurantIdInProductIds(new RestaurantId(restaurantId), List.of(product.getId()));

        Assertions.assertThat(result.size())
                .isEqualTo(1);
        Assertions.assertThat(result.get(0))
                .isEqualTo(product);
    }

    void insertProduct(Product product, UUID restaurantId) {
        final String q = "insert into restaurant.product(id, restaurant_id, quantity, price) values(?,?,?,?)";
        jdbcTemplate.update(q,
                product.getId().getValue(),
                restaurantId,
                product.getQuantity(),
                product.getPrice().getAmount());
    }
}