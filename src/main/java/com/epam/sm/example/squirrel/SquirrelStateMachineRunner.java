package com.epam.sm.example.squirrel;

import com.epam.sm.example.model.Order;
import com.epam.sm.example.model.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.epam.sm.example.model.DeliveryType.SHOP;

@Slf4j
@Profile("squirrel")
@Component
@RequiredArgsConstructor
public class SquirrelStateMachineRunner implements CommandLineRunner {

private final SquirrelStateMachineFactory
        squirrelStateMachineFactory;

@Override
public void run(String... args) {
    runSquirrelStateMachine();
}

private void runSquirrelStateMachine() {
    log.info("---Squirrel State Machine---");
    SquirrelStateMachine squirrelStateMachine =
            squirrelStateMachineFactory.getSquirrelStateMachine();

    final Order order = new Order()
            .setTotalSum(100);
    squirrelStateMachine.fire(OrderEvent.SUBMIT,
            SquirrelStateMachineContext.builder()
                    .order(order)
                    .build());

    squirrelStateMachine.fire(OrderEvent.PAY);

    squirrelStateMachine.fire(OrderEvent.READY,
            SquirrelStateMachineContext.builder()
                    .order(order)
                    .deliveryType(SHOP)
                    .build());

    squirrelStateMachine.fire(OrderEvent.COMPLETE);

    squirrelStateMachine.fire(OrderEvent.FULFILL);
}
}
