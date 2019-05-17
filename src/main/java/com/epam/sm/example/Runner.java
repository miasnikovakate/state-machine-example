package com.epam.sm.example;

import static com.epam.sm.example.model.DeliveryType.MAIL;
import static com.epam.sm.example.model.DeliveryType.SERVICE;
import static com.epam.sm.example.model.DeliveryType.SHOP;
import static com.epam.sm.example.ssm.SpringStateMachineConfig.DELIVERY_TYPE;

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

@Slf4j
@Component
@RequiredArgsConstructor
public class Runner implements ApplicationRunner {

    private final StateMachineFactory<OrderState, OrderEvent> stateMachineFactory;
    private final SquirrelStateMachineFactory squirrelStateMachineFactory;

    @Override
    public void run(ApplicationArguments args) {
        runSpringStateMachine();
        runSquirrelStateMachine();
        runAkkaStateMachine();
    }

    private void runSpringStateMachine() {
        log.info("---Spring State Machine---");
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
        log.info("---Squirrel State Machine---");
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
        log.info("---Akka State Machine---");
        ActorSystem system = ActorSystem.create("state-machine-example");
        ActorRef stateMachine = system.actorOf(Props.create(AkkaStateMachine.class, AkkaStateMachine::new));
        ActorRef transitionSubscriber = system.actorOf(Props.create(TransitionSubscriber.class));
        stateMachine.tell(new FSM.SubscribeTransitionCallBack(transitionSubscriber), null);

        stateMachine.tell(OrderEvent.SUBMIT, ActorRef.noSender());

        stateMachine.tell(OrderEvent.PAY, ActorRef.noSender());

        stateMachine.tell(OrderEvent.READY, ActorRef.noSender());

        stateMachine.tell(OrderMessage.builder()
                        .event(OrderEvent.READY)
                        .addVariable(DELIVERY_TYPE, MAIL)
                        .build(),
                ActorRef.noSender());

        stateMachine.tell(OrderEvent.COMPLETE, ActorRef.noSender());

        stateMachine.tell(OrderEvent.FULFILL, ActorRef.noSender());
    }
}
