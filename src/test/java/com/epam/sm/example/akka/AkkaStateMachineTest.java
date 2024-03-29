package com.epam.sm.example.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.FSM;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.epam.sm.example.model.OrderEvent;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.epam.sm.example.model.Constants.DELIVERY_TYPE;
import static com.epam.sm.example.model.DeliveryType.MAIL;

public class AkkaStateMachineTest {

private static ActorSystem SYSTEM;

@BeforeClass
public static void setup() {
    SYSTEM = ActorSystem.create("AkkaStateMachineTest");
}

@AfterClass
public static void tearDown() {
    TestKit.shutdownActorSystem(SYSTEM);
}

@Test
public void testSubmitAction() {
    new TestKit(SYSTEM) {
        {
            ActorRef stateMachine =
                    SYSTEM.actorOf(
                            Props.create(AkkaStateMachine.class,
                                    AkkaStateMachine::new));
            ActorRef sender = getRef();

            stateMachine.tell(getCallback(), ActorRef.noSender());

            final double orderSum = 100d;
            stateMachine.tell(orderSum, sender);
            expectNoMessage();

            stateMachine.tell(OrderEvent.SUBMIT, sender);
            expectMsg(
                    OrderData.builder()
                            .totalSum(orderSum)
                            .build());

            SYSTEM.stop(stateMachine);
        }
    };
}

@Test
public void testFulfilAction() throws InterruptedException {
    new TestKit(SYSTEM) {
        {
            ActorRef stateMachine =
                    SYSTEM.actorOf(
                            Props.create(AkkaStateMachine.class,
                                    AkkaStateMachine::new));
            ActorRef sender = getRef();

            stateMachine.tell(getCallback(), ActorRef.noSender());

            stateMachine.tell(100d, sender);

            stateMachine.tell(OrderEvent.SUBMIT, sender);
            expectMsg(
                    OrderData.builder()
                            .totalSum(100d)
                            .build());

            stateMachine.tell(OrderEvent.PAY, sender);

            stateMachine.tell(OrderEvent.PROCESS, sender);
            expectNoMessage();

            stateMachine.tell(OrderMessage.builder()
                            .event(OrderEvent.READY)
                            .addVariable(DELIVERY_TYPE, MAIL)
                            .build(),
                    sender);

            stateMachine.tell(OrderEvent.COMPLETE, sender);

            stateMachine.tell(OrderEvent.FULFILL, sender);
            expectMsg("Order is fulfilled");

            SYSTEM.stop(stateMachine);
        }
    };
}

private FSM.SubscribeTransitionCallBack getCallback() {
    ActorRef transitionSubscriber =
            SYSTEM.actorOf(Props.create(TransitionSubscriber.class));
    return new FSM.SubscribeTransitionCallBack(transitionSubscriber);
}
}