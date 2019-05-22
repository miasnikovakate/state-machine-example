package com.epam.sm.example.squirrel;

import com.epam.sm.example.model.Order;
import com.epam.sm.example.model.OrderEvent;
import com.epam.sm.example.model.OrderState;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.squirrelframework.foundation.fsm.StateMachinePerformanceMonitor;
import org.squirrelframework.foundation.fsm.StateMachineStatus;

import static com.epam.sm.example.model.DeliveryType.SHOP;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@ActiveProfiles("squirrel")
@SpringBootTest
public class SquirrelStateMachineTest {

@Autowired
private SquirrelStateMachineFactory squirrelStateMachineFactory;

@Test
public void testSubmitAction() {
    final SquirrelStateMachine squirrelStateMachine
            = squirrelStateMachineFactory.getSquirrelStateMachine();
    assertThat(squirrelStateMachine.getStatus(),
            equalTo(StateMachineStatus.INITIALIZED));

    squirrelStateMachine.start();
    assertThat(squirrelStateMachine.getStatus(),
            equalTo(StateMachineStatus.IDLE));
    assertThat(squirrelStateMachine.getCurrentState(),
            equalTo(OrderState.CREATED));

    final Order order = new Order()
            .setTotalSum(100);
    squirrelStateMachine.fire(OrderEvent.SUBMIT,
            SquirrelStateMachineContext.builder()
                    .order(order)
                    .build());

    assertThat(squirrelStateMachine.getCurrentState(),
            equalTo(OrderState.SUBMITTED));
}

@Test
public void testFulfillAction() throws InterruptedException {
    SquirrelStateMachine squirrelStateMachine =
            squirrelStateMachineFactory.getSquirrelStateMachine();
    final StateMachinePerformanceMonitor performanceMonitor =
            new StateMachinePerformanceMonitor(
                    "Squirrel State Machine Performance Info");
    squirrelStateMachine.addDeclarativeListener(performanceMonitor);

    final Order order = new Order()
            .setTotalSum(100);
    squirrelStateMachine.fire(OrderEvent.SUBMIT,
            SquirrelStateMachineContext.builder()
                    .order(order)
                    .build());

    squirrelStateMachine.fire(OrderEvent.PAY);

    Thread.sleep(2000);

    squirrelStateMachine.fire(OrderEvent.READY,
            SquirrelStateMachineContext.builder()
                    .order(order)
                    .deliveryType(SHOP)
                    .build());

    squirrelStateMachine.fire(OrderEvent.COMPLETE);

    squirrelStateMachine.fire(OrderEvent.FULFILL);

    squirrelStateMachine.removeDeclarativeListener(performanceMonitor);
    log.info("\n" + performanceMonitor.getPerfModel().toString());
}

}