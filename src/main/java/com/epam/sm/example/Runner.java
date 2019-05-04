package com.epam.sm.example;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.FSM;
import akka.actor.Props;
import com.epam.sm.example.akka.AkkaStateMachine;
import com.epam.sm.example.akka.OrderMessage;
import com.epam.sm.example.akka.TransitionSubscriber;
import com.epam.sm.example.model.OrderEvent;
import com.epam.sm.example.model.OrderState;
import com.epam.sm.example.squirrel.SquirrelStateMachine;
import com.epam.sm.example.squirrel.SquirrelStateMachineContext;
import com.epam.sm.example.squirrel.SquirrelStateMachineFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Component;

import static com.epam.sm.example.model.DeliveryType.*;
import static com.epam.sm.example.ssm.SpringStateMachineConfig.DELIVERY_TYPE;

@Slf4j
@Component
@RequiredArgsConstructor
public class Runner implements ApplicationRunner {

    private final StateMachineFactory<OrderState, OrderEvent> stateMachineFactory;
    private final SquirrelStateMachineFactory squirrelStateMachineFactory;

    @Override
    public void run(ApplicationArguments args) {
//        runSpringStateMachine();
//        runSquirrelStateMachine();
        runAkkaStateMachine();
    }

    private void runSpringStateMachine() {
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

    private void runSquirrelStateMachine() {
        SquirrelStateMachine squirrelStateMachine = squirrelStateMachineFactory.getSquirrelStateMachine();
        squirrelStateMachine.fire(OrderEvent.SUBMIT);

        squirrelStateMachine.fire(OrderEvent.PAY);

        squirrelStateMachine.fire(OrderEvent.READY);

        squirrelStateMachine.fire(OrderEvent.READY, SquirrelStateMachineContext.builder()
                .addVariable(DELIVERY_TYPE, SHOP)
                .build());

        squirrelStateMachine.fire(OrderEvent.COMPLETE);

        squirrelStateMachine.fire(OrderEvent.FULFILL);
    }

    private void runAkkaStateMachine() {
        ActorSystem system = ActorSystem.create("state-machine-example");
        ActorRef stateMachine = system.actorOf(Props.create(AkkaStateMachine.class)); // TODO: creator?
        ActorRef transitionSubscriber = system.actorOf(Props.create(TransitionSubscriber.class));
        stateMachine.tell(new FSM.SubscribeTransitionCallBack(transitionSubscriber), null);

        stateMachine.tell(OrderEvent.SUBMIT, null);

        stateMachine.tell(OrderEvent.PAY, null);

        stateMachine.tell(OrderEvent.READY, null);

        stateMachine.tell(OrderMessage.builder()
                        .event(OrderEvent.READY)
                        .addVariable(DELIVERY_TYPE, MAIL)
                        .build(),
                null);

        stateMachine.tell(OrderEvent.COMPLETE, null);

        stateMachine.tell(OrderEvent.FULFILL, null);
    }
}
