package com.epam.sm.example.ssm;

import com.epam.sm.example.model.Order;
import com.epam.sm.example.model.OrderEvent;
import com.epam.sm.example.model.OrderState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Component;

import static com.epam.sm.example.model.Constants.DELIVERY_TYPE;
import static com.epam.sm.example.model.Constants.ORDER_PARAMETER;
import static com.epam.sm.example.model.DeliveryType.SERVICE;

@Slf4j
@Profile("ssm")
@Component
@RequiredArgsConstructor
public class SpringStateMachineRunner implements CommandLineRunner {

private final
StateMachineFactory<OrderState, OrderEvent>
        stateMachineFactory;

@Override
public void run(String... args) {
    runSpringStateMachine();
}

private void runSpringStateMachine() {
    log.info("---Spring State Machine---");
    StateMachine<OrderState, OrderEvent> stateMachine =
            stateMachineFactory.getStateMachine();

    final Order order = new Order()
                                .setTotalSum(100);
    stateMachine.getExtendedState()
            .getVariables()
            .put(ORDER_PARAMETER, order);
    stateMachine.sendEvent(OrderEvent.SUBMIT);

    stateMachine.sendEvent(OrderEvent.PAY);

    Message<OrderEvent> readyMessage =
            MessageBuilder
                    .withPayload(OrderEvent.READY)
                    .setHeader(DELIVERY_TYPE, SERVICE)
                    .build();
    stateMachine.sendEvent(readyMessage);

    stateMachine.sendEvent(OrderEvent.COMPLETE);

    stateMachine.sendEvent(OrderEvent.FULFILL);
}
}
