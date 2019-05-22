package com.epam.sm.example.ssm;

import static com.epam.sm.example.model.Constants.DELIVERY_TYPE;
import static com.epam.sm.example.model.Constants.ORDER_PARAMETER;
import static com.epam.sm.example.model.DeliveryType.SERVICE;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.epam.sm.example.model.Order;
import com.epam.sm.example.model.OrderEvent;
import com.epam.sm.example.model.OrderState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("ssm")
@SpringBootTest
public class SpringStateMachineTest {

    @Autowired
    private
    StateMachineFactory<OrderState, OrderEvent>
            stateMachineFactory;

    @Test
    public void testSubmitAction() {
        final StateMachine<OrderState, OrderEvent> stateMachine
                = stateMachineFactory.getStateMachine();

        final Order order = new Order()
                .setTotalSum(100);
        stateMachine.getExtendedState()
                .getVariables()
                .put(ORDER_PARAMETER, order);

        stateMachine.sendEvent(OrderEvent.SUBMIT);

        assertThat(stateMachine.getState().getId(), equalTo(OrderState.SUBMITTED));
    }

    @Test
    public void testPlanSubmitAction() throws Exception {
        final StateMachine<OrderState, OrderEvent> stateMachine
                = stateMachineFactory.getStateMachine();

        final Order order = new Order()
                .setTotalSum(100);
        stateMachine.getExtendedState()
                .getVariables()
                .put(ORDER_PARAMETER, order);

        StateMachineTestPlan<OrderState, OrderEvent> testPlan =
                StateMachineTestPlanBuilder.<OrderState, OrderEvent>builder()
                        .stateMachine(stateMachine)
                        .step()
                            .expectStates(OrderState.CREATED)

                        .and()
                        .step()
                            .sendEvent(OrderEvent.SUBMIT)
                                .expectStateChanged(1)
                                .expectStates(OrderState.SUBMITTED)
                                .expectVariable(ORDER_PARAMETER)
                                .expectVariable(ORDER_PARAMETER, order)

                        .and()
                        .build();
        testPlan.test();
    }

    @Test
    public void testFulfilAction() throws Exception {
        final StateMachine<OrderState, OrderEvent> stateMachine
                = stateMachineFactory.getStateMachine();

        final Order order = new Order()
                .setTotalSum(100);
        stateMachine.getExtendedState()
                .getVariables()
                .put(ORDER_PARAMETER, order);

        StateMachineTestPlan<OrderState, OrderEvent> testPlan =
                StateMachineTestPlanBuilder.<OrderState, OrderEvent>builder()
                        .stateMachine(stateMachine)
                        .step()
                            .expectState(OrderState.CREATED)

                        .and()
                        .step()
                            .sendEvent(OrderEvent.SUBMIT)
                                .expectStateChanged(1)
                                .expectState(OrderState.SUBMITTED)
                                .expectVariable(ORDER_PARAMETER)
                                .expectVariable(ORDER_PARAMETER, order)

                        .and()
                        .step()
                            .sendEvent(OrderEvent.PAY)
                                .expectStateChanged(2)
                                .expectState(OrderState.PROCESSING)

                        .and()
                        .step()
                            .sendEvent(
                                    MessageBuilder
                                            .withPayload(OrderEvent.READY)
                                            .setHeader(DELIVERY_TYPE, SERVICE)
                                            .build())
                                .expectStateChanged(1)
                                .expectState(OrderState.SERVICE_DELIVERY)

                        .and()
                        .step()
                            .sendEvent(OrderEvent.COMPLETE)
                                .expectStateChanged(1)
                                .expectState(OrderState.DELIVERED)

                        .and()
                        .step()
                            .sendEvent(OrderEvent.FULFILL)
                                .expectStateChanged(1)
                                .expectStates(OrderState.FULFILLED)

                        .and()
                        .build();
        testPlan.test();
    }
}