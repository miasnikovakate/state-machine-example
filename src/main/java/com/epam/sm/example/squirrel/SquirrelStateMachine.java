package com.epam.sm.example.squirrel;

import com.epam.sm.example.model.OrderEvent;
import com.epam.sm.example.model.OrderState;
import lombok.extern.slf4j.Slf4j;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

@Slf4j
public class SquirrelStateMachine
        extends AbstractStateMachine<SquirrelStateMachine,
                                            OrderState,
                                            OrderEvent,
                                            SquirrelStateMachineContext> {

public void submitAction(OrderState from,
                         OrderState to,
                         OrderEvent event,
                         SquirrelStateMachineContext context) {
    context.getOrder()
            .setReadOnly(true);
}

public void transitFromSUBMITTEDToPAIDOnPAY(OrderState from,
                                            OrderState to,
                                            OrderEvent event,
                                            SquirrelStateMachineContext context) {
    this.fire(OrderEvent.PROCESS);
}

public void entryPROCESSING(OrderState from,
                            OrderState to,
                            OrderEvent event,
                            SquirrelStateMachineContext context) {
    log.warn("Start processing order");
}

public void exitPROCESSING(OrderState from,
                           OrderState to,
                           OrderEvent event,
                           SquirrelStateMachineContext context) {
    log.warn("Finish processing order");
}

public void processingOrder(OrderState from,
                            OrderState to,
                            OrderEvent event,
                            SquirrelStateMachineContext context) {
    log.warn("Processing order");
}
}