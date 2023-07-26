package com.sangjun.order.domain.valueobject;

import java.util.Objects;

public class StreetAddress {
    private final String street;
    private final String postalCode;
    private final String city;

    public StreetAddress(String street, String postalCode, String city) {
        this.street = street;
        this.postalCode = postalCode;
        this.city = city;
    }

    private StreetAddress(Builder builder) {
        street = builder.street;
        postalCode = builder.postalCode;
        city = builder.city;
    }

    public static Builder builder() {
        return new Builder();
    }


    public String getStreet() {
        return street;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCity() {
        return city;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StreetAddress that = (StreetAddress) o;
        return Objects.equals(street, that.street) && Objects.equals(postalCode, that.postalCode) && Objects.equals(city, that.city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, postalCode, city);
    }


    public static final class Builder {
        private String street;
        private String postalCode;
        private String city;

        private Builder() {
        }

        public Builder street(String val) {
            street = val;
            return this;
        }

        public Builder postalCode(String val) {
            postalCode = val;
            return this;
        }

        public Builder city(String val) {
            city = val;
            return this;
        }

        public StreetAddress build() {
            return new StreetAddress(this);
        }
    }
}
