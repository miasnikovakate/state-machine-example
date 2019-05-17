package com.epam.sm.example.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Order {
    private DeliveryType deliveryType;
    private Map<String, Object> things = new HashMap<>();

    public void addThing(String name, Object thing) {
        things.put(name, thing);
    }
}
