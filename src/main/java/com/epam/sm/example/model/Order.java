package com.epam.sm.example.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Order {
    private double totalSum;
    private boolean isReadOnly;
}
