package com.epam.sm.example.akka;

import com.epam.sm.example.model.OrderEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@ToString
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderMessage {
    private final OrderEvent orderEvent;
    private final Map<String, Object> contextVariables;

    public Object getVariable(String key) {
        return contextVariables.get(key);
    }

    public static OrderMessageBuilder builder() {
        return new OrderMessageBuilder();
    }

    public static class OrderMessageBuilder {
        private OrderEvent orderEvent;
        private Map<String, Object> contextVariables;

        OrderMessageBuilder() {
            contextVariables = new HashMap<>();
        }

        public OrderMessageBuilder addVariable(String key, Object var) {
            this.contextVariables.put(key, var);
            return this;
        }

        public OrderMessageBuilder event(OrderEvent orderEvent) {
            this.orderEvent = orderEvent;
            return this;
        }

        public OrderMessage build() {
            return new OrderMessage(orderEvent, contextVariables);
        }
    }
}
