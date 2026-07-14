package com.a0000.digicompass.modules.product.entity;

public record ProductSpec(Long id, String group, String name, String value, int sortOrder) {
    public ProductSpec(String group, String name, String value) {
        this(null, group, name, value, 0);
    }
}
