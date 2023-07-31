package com.sangjun.order.domain.service.ports.output.service;

import com.sangjun.order.domain.service.ports.output.service.product.ProductValidationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = ProductValidationTestConfig.class)
class ProductValidationServiceTest {

    @Autowired
    private ProductValidationService productValidationService;

    @Test
    void 제품_id와_가격이_모두_일치하면_통과() {
        //given
//        ProductId productId = new ProductId(UUID.randomUUID());
//        Money price = Money.of("3000");
//        int quantity = 3;
//
//        //when
//        ProductValidationRequest request = ProductValidationRequest.builder()
//                .productId(productId)
//                .price(price)
//                .quantity(quantity)
//                .build();
//
//        ProductValidationResponse response = productValidationService.validateProducts(List.of(request));
//
//        //then
//        Assertions.assertThat(response.isSuccessful())
//                .isTrue();
    }

}