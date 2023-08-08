package com.sangjun.restaurant.application;

import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.ProductId;
import com.sangjun.restaurant.application.dto.ProductDto;
import com.sangjun.restaurant.application.dto.RestaurantApprovalRequest;
import com.sangjun.restaurant.application.ports.input.message.listener.RestaurantApprovalRequestMessageListener;
import com.sangjun.restaurant.application.ports.output.message.repository.PendingOrderRepository;
import com.sangjun.restaurant.application.ports.output.message.repository.ProductRepository;
import com.sangjun.restaurant.domain.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sangjun.restaurant.application.mapper.RestaurantApplicationMapper.MAPPER;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantApprovalRequestMessageListenerImpl implements RestaurantApprovalRequestMessageListener {
    private final OrderApprovalEventShooter orderApprovalEventShooter;
    private final PendingOrderRepository pendingOrderRepository;
    private final ProductRepository productRepository;

    @Override
    public void registerPendingOrder(RestaurantApprovalRequest restaurantApprovalRequest) {
        validatePrice(restaurantApprovalRequest);
        var pendingOrder = MAPPER.toPendingOrder(restaurantApprovalRequest);
        pendingOrderRepository.save(pendingOrder);
    }

    private void validatePrice(RestaurantApprovalRequest restaurantApprovalRequest) {
        List<ProductDto> productDtos = restaurantApprovalRequest.getProducts();
        Map<ProductId, Product> productMap = toMap(productRepository.findAllByIdIn(toProductIds(productDtos)));
        Money total = computeTotalPrice(productDtos, productMap);

        if (isDifferent(restaurantApprovalRequest.getPrice(), total.getAmount())) {
            throw new IllegalArgumentException(String.format("Given price: %s is not equal to the actual total price: %s",
                    restaurantApprovalRequest.getPrice(), total));
        }
    }

    private List<ProductId> toProductIds(List<ProductDto> products) {
        return products.stream().map(ProductDto::getProductId).toList();
    }

    private Map<ProductId, Product> toMap(List<Product> products) {
        return products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
    }

    private Money computeTotalPrice(List<ProductDto> productDtos, Map<ProductId, Product> productMap) {
        Money total = Money.ZERO;
        for (var productDto : productDtos) {
            Product product = productMap.get(productDto.getProductId());
            Money subTotal = product.getPrice()
                    .multiply(productDto.getQuantity());
            total = total.add(subTotal);
        }
        return total;
    }

    private boolean isDifferent(BigDecimal givenPrice, BigDecimal actualPrice) {
        return !actualPrice.equals(givenPrice);
    }
}
