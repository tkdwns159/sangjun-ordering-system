package com.sangjun.common.domain.valueobject;

import javax.persistence.*;
import java.util.UUID;

@Embeddable
@AttributeOverride(name = "value", column = @Column(name = "restaurant_id"))
@Access(AccessType.FIELD)
public class RestaurantId extends BaseId<UUID> {
    public RestaurantId(UUID value) {
        super(value);
    }

    protected RestaurantId() {
    }
}
