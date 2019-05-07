package com.epam.sm.example.akka;

import akka.actor.AbstractFSM;
import com.epam.sm.example.model.DeliveryType;
import com.epam.sm.example.model.OrderEvent;
import com.epam.sm.example.model.OrderState;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

import static com.epam.sm.example.ssm.SpringStateMachineConfig.DELIVERY_TYPE;

@Slf4j
public class AkkaStateMachine extends AbstractFSM<OrderState, AkkaData> {

    public AkkaStateMachine() {
        startWith(OrderState.CREATED, new AkkaData() {
        });

        when(OrderState.CREATED,
                matchEventEquals(OrderEvent.SUBMIT, (event, data) -> goTo(OrderState.SUBMITTED)));
        onTransition(matchState(OrderState.CREATED, OrderState.SUBMITTED, () -> submitAction(stateData())));

        when(OrderState.SUBMITTED,
                matchEventEquals(OrderEvent.PAY, (event, data) -> goTo(OrderState.PAID))
                        .eventEquals(OrderEvent.CANCEL, (event, data) -> goTo(OrderState.CANCELLED)));

        when(OrderState.PAID, Duration.ZERO, matchAnyEvent((event, data) -> goTo(OrderState.PROCESSING)));

        when(OrderState.PROCESSING,
                matchEvent(OrderMessage.class,
                        (event, data) -> event.getOrderEvent() == OrderEvent.READY
                                && event.getVariable(DELIVERY_TYPE) == DeliveryType.MAIL,
                        (event, data) -> goTo(OrderState.MAIL_DELIVERY))
                        .event(OrderMessage.class,
                                (event, data) -> event.getOrderEvent() == OrderEvent.READY
                                        && event.getVariable(DELIVERY_TYPE) == DeliveryType.SERVICE,
                                (event, data) -> goTo(OrderState.SERVICE_DELIVERY))
                        .event(OrderMessage.class,
                                (event, data) -> event.getOrderEvent() == OrderEvent.READY
                                        && event.getVariable(DELIVERY_TYPE) == DeliveryType.SHOP,
                                (event, data) -> goTo(OrderState.DELIVERY_TO_SHOP)));


        when(OrderState.MAIL_DELIVERY,
                matchEventEquals(OrderEvent.COMPLETE, (event, data) -> goTo(OrderState.DELIVERED)));
        when(OrderState.SERVICE_DELIVERY,
                matchEventEquals(OrderEvent.COMPLETE, (event, data) -> goTo(OrderState.DELIVERED)));
        when(OrderState.DELIVERY_TO_SHOP,
                matchEventEquals(OrderEvent.COMPLETE, (event, data) -> goTo(OrderState.DELIVERED)));

        when(OrderState.DELIVERED,
                matchEventEquals(OrderEvent.FULFILL, (event, data) -> goTo(OrderState.FULFILLED)));
        when(OrderState.DELIVERED,
                matchEventEquals(OrderEvent.CANCEL, (event, data) -> goTo(OrderState.CANCELLED)));

        when(OrderState.FULFILLED, NullFunction());
        when(OrderState.CANCELLED, NullFunction());

        whenUnhandled(matchAnyEvent((event, state) -> {
            log.warn("event - {}, state - {}", event, stateName());
            return stay();
        }));
    }

    private void submitAction(AkkaData data) {
    }
}
