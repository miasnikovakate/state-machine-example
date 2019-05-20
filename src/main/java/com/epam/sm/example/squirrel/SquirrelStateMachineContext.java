package com.epam.sm.example.squirrel;

import com.epam.sm.example.model.DeliveryType;
import com.epam.sm.example.model.Order;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SquirrelStateMachineContext {
    private Order order;
    private DeliveryType deliveryType;
}
