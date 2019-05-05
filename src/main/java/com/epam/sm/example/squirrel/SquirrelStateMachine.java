package com.epam.sm.example.squirrel;

import com.epam.sm.example.model.OrderEvent;
import com.epam.sm.example.model.OrderState;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

public class SquirrelStateMachine
        extends AbstractStateMachine<SquirrelStateMachine, OrderState, OrderEvent, SquirrelStateMachineContext> {

    public void submitAction(OrderState from, OrderState to, OrderEvent event, SquirrelStateMachineContext context) {
    }
}
