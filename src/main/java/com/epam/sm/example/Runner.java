package com.epam.sm.example;

import com.epam.sm.example.model.OrderEvent;
import com.epam.sm.example.model.OrderState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Component;

import static com.epam.sm.example.model.DeliveryType.SERVICE;
import static com.epam.sm.example.ssm.SpringStateMachineConfig.DELIVERY_TYPE;

@Slf4j
@Component
@RequiredArgsConstructor
public class Runner implements ApplicationRunner {

    private final StateMachineFactory<OrderState, OrderEvent> stateMachineFactory;

    @Override
    public void run(ApplicationArguments args) {
        StateMachine<OrderState, OrderEvent> stateMachine = stateMachineFactory.getStateMachine();
        stateMachine.sendEvent(OrderEvent.SUBMIT);

        stateMachine.sendEvent(OrderEvent.PAY);

        Message<OrderEvent> readyMessage = MessageBuilder
                .withPayload(OrderEvent.READY)
                .setHeader(DELIVERY_TYPE, SERVICE)
                .build();
        stateMachine.sendEvent(readyMessage);

        stateMachine.sendEvent(OrderEvent.COMPLETE);

        stateMachine.sendEvent(OrderEvent.FULFILL);
    }
}
