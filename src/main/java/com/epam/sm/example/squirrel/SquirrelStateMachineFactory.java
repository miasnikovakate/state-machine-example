package com.epam.sm.example.squirrel;

import com.epam.sm.example.model.DeliveryType;
import com.epam.sm.example.model.Order;
import com.epam.sm.example.model.OrderEvent;
import com.epam.sm.example.model.OrderState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.squirrelframework.foundation.fsm.AnonymousCondition;
import org.squirrelframework.foundation.fsm.Condition;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;

import javax.annotation.PostConstruct;
import java.util.Objects;

@Slf4j
@Profile("squirrel")
@Component
public class SquirrelStateMachineFactory {

private StateMachineBuilder<SquirrelStateMachine,
                                   OrderState,
                                   OrderEvent,
                                   SquirrelStateMachineContext> builder;

@PostConstruct
public void init() {
    this.builder = StateMachineBuilderFactory.create(
            SquirrelStateMachine.class,
            OrderState.class,
            OrderEvent.class,
            SquirrelStateMachineContext.class);
    configureSquirrelStateMachine();
}

private void configureSquirrelStateMachine() {
    builder.externalTransition()
            .from(OrderState.CREATED)
            .to(OrderState.SUBMITTED)
            .on(OrderEvent.SUBMIT)
            .when(isOrderValid())
            .callMethod("submitAction");

    builder.externalTransition()
            .from(OrderState.SUBMITTED)
            .to(OrderState.PAID)
            .on(OrderEvent.PAY);

    builder.externalTransition()
            .from(OrderState.SUBMITTED)
            .to(OrderState.CANCELLED)
            .on(OrderEvent.CANCEL);

    builder.externalTransition()
            .from(OrderState.PAID)
            .to(OrderState.PROCESSING)
            .on(OrderEvent.READY);

    builder.externalTransition()
            .from(OrderState.PROCESSING)
            .to(OrderState.MAIL_DELIVERY).on(OrderEvent.READY)
            .when(new AnonymousCondition<SquirrelStateMachineContext>() {
                @Override
                public boolean isSatisfied(SquirrelStateMachineContext context) {
                    DeliveryType deliveryType = context.getDeliveryType();
                    return Objects.nonNull(deliveryType)
                                   && deliveryType == DeliveryType.MAIL;
                }
            });

    builder.externalTransition()
            .from(OrderState.PROCESSING)
            .to(OrderState.SERVICE_DELIVERY)
            .on(OrderEvent.READY)
            .when(new AnonymousCondition<SquirrelStateMachineContext>() {
                @Override
                public boolean isSatisfied(SquirrelStateMachineContext context) {
                    DeliveryType deliveryType = context.getDeliveryType();
                    return Objects.nonNull(deliveryType)
                                   && deliveryType == DeliveryType.SERVICE;
                }
            });

    builder.externalTransition()
            .from(OrderState.PROCESSING)
            .to(OrderState.DELIVERY_TO_SHOP)
            .on(OrderEvent.READY);

    builder.externalTransition()
            .from(OrderState.MAIL_DELIVERY)
            .to(OrderState.DELIVERED)
            .on(OrderEvent.COMPLETE);
    builder.externalTransition()
            .from(OrderState.SERVICE_DELIVERY)
            .to(OrderState.DELIVERED)
            .on(OrderEvent.COMPLETE);
    builder.externalTransition()
            .from(OrderState.DELIVERY_TO_SHOP)
            .to(OrderState.DELIVERED)
            .on(OrderEvent.COMPLETE);

    builder.externalTransition()
            .from(OrderState.DELIVERED)
            .to(OrderState.FULFILLED)
            .on(OrderEvent.FULFILL);
}

public SquirrelStateMachine getSquirrelStateMachine() {
    SquirrelStateMachine squirrelStateMachine =
            builder.newStateMachine(OrderState.CREATED);
    squirrelStateMachine
            .addTransitionCompleteListener(
                    event -> log.info("State changed from {} to {}",
                            event.getSourceState(), event.getTargetState()));
    return squirrelStateMachine;
}

private Condition<SquirrelStateMachineContext> isOrderValid() {
    return new Condition<SquirrelStateMachineContext>() {
        @Override
        public boolean isSatisfied(SquirrelStateMachineContext context) {
            Order order = context.getOrder();
            return Objects.nonNull(order) && order.getTotalSum() > 0;
        }

        @Override
        public String name() {
            return "Is current order valid?";
        }
    };
}
}
