package com.sangjun.order.dataaccess.customer;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.order.domain.entity.Customer;
import com.sangjun.order.domain.service.ports.output.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CustomerJpaRepository implements CustomerRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Optional<Customer> findById(CustomerId customerId) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", customerId.getValue());

        final String query = "SELECT id FROM customer.customers WHERE id=:id";
        Customer result = jdbcTemplate.queryForObject(query, params, (rs, rowNum) ->
                new Customer(new CustomerId(UUID.fromString(rs.getString("id")))));

        return Optional.ofNullable(result);
    }
}
