package com.epam.sm.example.akka;

import akka.actor.AbstractActor;
import akka.actor.FSM;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransitionSubscriber extends AbstractActor {
@Override
public Receive createReceive() {
    return receiveBuilder()
           .match(
                   FSM.CurrentState.class,
                   currentState -> log.info("initial state - {}",
                           currentState.state())
           )
           .match(
                   FSM.Transition.class,
                   transition -> log.info("transition from {} to {}",
                           transition.from(), transition.to())
           )
           .build();
}
}
