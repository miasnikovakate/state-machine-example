package com.epam.sm.example.akka;

import akka.actor.*;
import com.epam.sm.example.model.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.epam.sm.example.model.Constants.DELIVERY_TYPE;
import static com.epam.sm.example.model.DeliveryType.MAIL;

@Slf4j
@Profile("akka")
@Component
@RequiredArgsConstructor
public class AkkaStateMachineRunner implements CommandLineRunner {

@Override
public void run(String... args) {
    runAkkaStateMachine();
}

private void runAkkaStateMachine() {
    log.info("---Akka State Machine---");
    ActorSystem system = ActorSystem.create("state-machine-example");
    ActorRef sender =
            system.actorOf(Props.create(SenderActor.class));

    ActorRef stateMachine =
            system.actorOf(
                    Props.create(AkkaStateMachine.class,
                            AkkaStateMachine::new));

    ActorRef transitionSubscriber =
            system.actorOf(Props.create(TransitionSubscriber.class));
    stateMachine.tell(
            new FSM.SubscribeTransitionCallBack(transitionSubscriber),
            ActorRef.noSender());

    stateMachine.tell(100d, sender);

    stateMachine.tell(OrderEvent.SUBMIT, sender);

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

private static class SenderActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                       .matchAny(
                               message ->
                                       log.info("Received message from SM: {}",
                                                 message))
                       .build();
    }
}
}
