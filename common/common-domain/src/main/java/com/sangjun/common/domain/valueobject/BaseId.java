package com.sangjun.common.domain.valueobject;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Objects;

@MappedSuperclass
@Access(AccessType.FIELD)
public abstract class BaseId<T> implements Serializable {

    private T value;

    protected BaseId(T value) {
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }

    protected BaseId() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseId<?> baseId = (BaseId<?>) o;
        return Objects.equals(value, baseId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
