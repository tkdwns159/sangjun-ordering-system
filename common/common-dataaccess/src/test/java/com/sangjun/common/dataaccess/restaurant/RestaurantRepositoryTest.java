package com.sangjun.common.dataaccess.restaurant;


import com.sangjun.common.dataaccess.restaurant.entity.RestaurantEntity;
import com.sangjun.common.dataaccess.restaurant.exception.RestaurantDataAccessException;
import com.sangjun.common.dataaccess.restaurant.repository.RestaurantJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.transaction.AfterTransaction;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Sql(scripts = "classpath:restaurant-testdata.sql")
@ActiveProfiles("test")
@Transactional
@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2,
        replace = AutoConfigureTestDatabase.Replace.NONE)
public class RestaurantRepositoryTest {

    private static final UUID RESTAURANT_ID_1 = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb45");
    //    private static final UUID RESTAURANT_ID_2 = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb46");
    private static final UUID PRODUCT_ID_1 = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb47");
    private static final UUID PRODUCT_ID_2 = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb48");
    //    private static final UUID PRODUCT_ID_3 = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb49");

    //    private static final UUID PRODUCT_ID_4 = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb50");

    public static final List<UUID> RESTAURANT_PRODUCT_ID_1_LIST = List.of(PRODUCT_ID_1, PRODUCT_ID_2);
    //    public static final List<UUID> RESTAURANT_PRODUCT_ID_2_LIST = List.of(PRODUCT_ID_3, PRODUCT_ID_4);

    @Autowired
    private RestaurantJpaRepository restaurantJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void contextLoads() {
    }

    @AfterTransaction
    void clearContext() {
        entityManager.clear();
    }

    @Test
    void testFindByRestaurantIdAndProductIdIn() {
        Optional<List<RestaurantEntity>> result = restaurantJpaRepository.findByRestaurantIdAndProductIdIn(RESTAURANT_ID_1, RESTAURANT_PRODUCT_ID_1_LIST);

        List<RestaurantEntity> foundRestaurants = assertDoesNotThrow(() -> result
                .orElseThrow(() -> new RestaurantDataAccessException("Restaurant not found")));

        assertEquals(RESTAURANT_PRODUCT_ID_1_LIST.size(), foundRestaurants.size());

        RESTAURANT_PRODUCT_ID_1_LIST
                .forEach(id -> assertTrue(foundRestaurants.stream()
                        .anyMatch(restaurant -> restaurant.getProductId().equals(id))));
    }
}
