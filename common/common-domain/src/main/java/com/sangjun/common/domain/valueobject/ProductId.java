package com.sangjun.common.domain.valueobject;

import javax.persistence.*;
import java.util.UUID;

@Embeddable
@Access(AccessType.FIELD)
@AttributeOverride(name = "value", column = @Column(name = "product_id"))
public class ProductId extends BaseId<UUID> {
    public ProductId(UUID value) {
        super(value);
    }

    protected ProductId() {
    }
}
