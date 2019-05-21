package com.epam.sm.example.squirrel;

import static com.epam.sm.example.model.DeliveryType.SHOP;

import com.epam.sm.example.model.Order;
import com.epam.sm.example.model.OrderEvent;
import com.epam.sm.example.model.OrderState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.squirrelframework.foundation.fsm.ObjectSerializableSupport;
import org.squirrelframework.foundation.fsm.StateMachineData;

import javax.persistence.EntityNotFoundException;

@Slf4j
@Profile("squirrel")
@Component
@RequiredArgsConstructor
public class SquirrelStateMachineRunner implements CommandLineRunner {

    private final SquirrelStateMachineFactory squirrelStateMachineFactory;
    private final SquirrelStateMachineRepository squirrelStateMachineRepository;

    @Override
    public void run(String... args) {
        runAndSaveSquirrelStateMachine();
        restoreStateMachine();
    }

    private void runAndSaveSquirrelStateMachine() {
        log.info("---Squirrel State Machine---");
        SquirrelStateMachine squirrelStateMachine =
                squirrelStateMachineFactory.getSquirrelStateMachine();

        final Order order = new Order()
                .setTotalSum(100);
        squirrelStateMachine.fire(OrderEvent.SUBMIT,
                SquirrelStateMachineContext.builder()
                        .order(order)
                        .build());

        StateMachineData.Reader<SquirrelStateMachine,
                OrderState,
                OrderEvent,
                SquirrelStateMachineContext>
                savedData = squirrelStateMachine.dumpSavedData();

        SquirrelStateMachineEntity squirrelStateMachineEntity =
                new SquirrelStateMachineEntity(
                        squirrelStateMachine.getIdentifier(),
                        ObjectSerializableSupport.serialize(savedData));
        squirrelStateMachineRepository.save(squirrelStateMachineEntity);
    }

    private void restoreStateMachine() {
        log.info("---Squirrel State Machine Restore---");
        final String id = "dc3YyqIUS6";
        final SquirrelStateMachineEntity squirrelStateMachineEntity
                = squirrelStateMachineRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Not found Squirrel State Machine with id = " + id));

        SquirrelStateMachine squirrelStateMachine =
                squirrelStateMachineFactory.getSquirrelStateMachine();

        squirrelStateMachine.loadSavedData(
                ObjectSerializableSupport.deserialize(
                        squirrelStateMachineEntity.getData()));

        squirrelStateMachine.fire(OrderEvent.PAY);

        squirrelStateMachine.fire(OrderEvent.READY);

        final Order order = new Order()
                .setTotalSum(100);
        squirrelStateMachine.fire(OrderEvent.READY,
                SquirrelStateMachineContext.builder()
                        .order(order)
                        .deliveryType(SHOP)
                        .build());

        squirrelStateMachine.fire(OrderEvent.COMPLETE);

        squirrelStateMachine.fire(OrderEvent.FULFILL);
    }
}
