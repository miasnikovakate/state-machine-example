package com.epam.sm.example.ssm;

import static com.epam.sm.example.model.OrderState.CREATED;
import static com.epam.sm.example.model.OrderState.PAID;
import static com.epam.sm.example.model.OrderState.SUBMITTED;

import com.epam.sm.example.model.DeliveryType;
import com.epam.sm.example.model.OrderEvent;
import com.epam.sm.example.model.OrderState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Random;

@Slf4j
@Configuration
@EnableStateMachineFactory
public class SpringStateMachineConfig extends EnumStateMachineConfigurerAdapter<OrderState, OrderEvent> {
    public static final String DELIVERY_TYPE = "DELIVERY_TYPE";
    public static final Random RANDOM = new Random();

    @Override
    public void configure(StateMachineConfigurationConfigurer<OrderState, OrderEvent> config) throws Exception {
        config
                .withConfiguration()
                .autoStartup(true)
                .listener(logListener())
                .taskExecutor(taskExecutor());
    }

    @Override
    public void configure(StateMachineStateConfigurer<OrderState, OrderEvent> states) throws Exception {
        states
                .withStates()
                .initial(CREATED)
                .states(EnumSet.of(
                        OrderState.CREATED,
                        OrderState.SUBMITTED,
                        OrderState.PAID,
                        OrderState.PROCESSING,
                        OrderState.MAIL_DELIVERY,
                        OrderState.SERVICE_DELIVERY,
                        OrderState.DELIVERY_TO_SHOP,
                        OrderState.DELIVERED,
                        OrderState.FULFILLED,
                        OrderState.CANCELLED,
                        OrderState.PACKAGED))
                .choice(OrderState.DELIVERY_CHOICE)
                .fork(OrderState.FORK_PROCESSING)
                .join(OrderState.JOIN_PACKAGED)
                .and()
                .withStates()
                .region("booking")
                .parent(OrderState.PROCESSING)
                .initial(OrderState.PR_1_BOOKING_THINGS)
                .end(OrderState.PR_1_BOOKED_THINGS)
                .and()
                .withStates()
                .region("ordering")
                .parent(OrderState.PROCESSING)
                .initial(OrderState.PR_2_ORDERING_THINGS)
                .states(EnumSet.of(OrderState.PR_2_ORDERED_THINGS, OrderState.PR_2_WAITING_THINGS))
                .end(OrderState.PR_2_DELIVERED_THINGS);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderState, OrderEvent> transitions) throws Exception {
        transitions.withExternal()
                .source(CREATED).target(SUBMITTED).event(OrderEvent.SUBMIT).action(submitAction())
                .and()
                .withExternal()
                .source(SUBMITTED).target(PAID).event(OrderEvent.PAY)
                .and()
                .withExternal()
                .source(SUBMITTED).target(OrderState.CANCELLED).event(OrderEvent.CANCEL)
                .and()
                .withExternal()
                .source(PAID).target(OrderState.FORK_PROCESSING)
                .and()

                /*Parallel regions*/
                .withFork()
                .source(OrderState.FORK_PROCESSING).target(OrderState.PROCESSING)
                .and()
                .withInternal()
                .source(OrderState.PR_1_BOOKING_THINGS)
                .action(bookingThings())
                .timer(5000L)
                .and()
                .withExternal()
                .source(OrderState.PR_1_BOOKING_THINGS).target(OrderState.PR_1_BOOKED_THINGS).event(OrderEvent.PR_1_BOOK)
                .and()
                .withExternal()
                .source(OrderState.PR_2_ORDERING_THINGS).target(OrderState.PR_2_ORDERED_THINGS).event(OrderEvent.PR_2_ORDER)
                .and()
                .withExternal()
                .source(OrderState.PR_2_ORDERED_THINGS).target(OrderState.PR_2_WAITING_THINGS)
                .and()
                .withInternal()
                .source(OrderState.PR_2_WAITING_THINGS)
                .action(checkOrderedThings())
                .timer(10000L)
                .and()
                .withExternal()
                .source(OrderState.PR_2_WAITING_THINGS).target(OrderState.PR_2_DELIVERED_THINGS).event(OrderEvent.PR_2_COMPLETE)
                .and()
                .withJoin()
                .source(OrderState.PROCESSING).target(OrderState.JOIN_PACKAGED)
                .and()
                .withExternal()
                .source(OrderState.JOIN_PACKAGED).target(OrderState.PACKAGED)
                /*Parallel regions*/

                .and()
                .withExternal()
                .source(OrderState.PACKAGED).target(OrderState.DELIVERY_CHOICE).event(OrderEvent.READY)
                .and()
                .withChoice()
                .source(OrderState.DELIVERY_CHOICE)
                .first(OrderState.MAIL_DELIVERY, isMailDeliveryType())
                .then(OrderState.SERVICE_DELIVERY, isDeliveryServiceDeliveryType())
                .last(OrderState.DELIVERY_TO_SHOP)
                .and()
                .withExternal()
                .source(OrderState.MAIL_DELIVERY).target(OrderState.DELIVERED).event(OrderEvent.COMPLETE)
                .and()
                .withExternal()
                .source(OrderState.SERVICE_DELIVERY).target(OrderState.DELIVERED).event(OrderEvent.COMPLETE)
                .and()
                .withExternal()
                .source(OrderState.DELIVERY_TO_SHOP).target(OrderState.DELIVERED).event(OrderEvent.COMPLETE)
                .and()
                .withExternal()
                .source(OrderState.DELIVERED).target(OrderState.FULFILLED).event(OrderEvent.FULFILL)
                .and()
                .withExternal()
                .source(OrderState.DELIVERED).target(OrderState.CANCELLED).event(OrderEvent.CANCEL);
    }


    @Bean
    public Guard<OrderState, OrderEvent> isMailDeliveryType() {
        return stateContext -> {
            DeliveryType deliveryType = (DeliveryType) stateContext.getMessage().getHeaders().get(DELIVERY_TYPE);
            return Objects.nonNull(deliveryType) && deliveryType == DeliveryType.MAIL;
        };
    }

    @Bean
    public Guard<OrderState, OrderEvent> isDeliveryServiceDeliveryType() {
        return stateContext -> {
            DeliveryType deliveryType = (DeliveryType) stateContext.getMessage().getHeaders().get(DELIVERY_TYPE);
            return Objects.nonNull(deliveryType) && deliveryType == DeliveryType.SERVICE;
        };
    }

    @Bean
    public StateMachineListener<OrderState, OrderEvent> logListener() {
        return new StateMachineListenerAdapter<OrderState, OrderEvent>() {
            @Override
            public void stateChanged(State<OrderState, OrderEvent> from, State<OrderState, OrderEvent> to) {
                if (Objects.nonNull(from) && Objects.nonNull(to)) {
                    log.info("State changed from {} to {}", from.getId(), to.getId());
                }
            }

            @Override
            public void stateEntered(State<OrderState, OrderEvent> state) {
                log.info("Entered state: {}", state);
            }

            @Override
            public void stateExited(State<OrderState, OrderEvent> state) {
                log.info("Exited state: {}", state);
            }
        };
    }

    @Bean
    public Action<OrderState, OrderEvent> submitAction() {
        return context -> {
        };
    }


    @Bean
    public Action<OrderState, OrderEvent> bookingThings() {
        return context -> {
            final int randomInt = RANDOM.nextInt();
            log.info("booking things: {}", randomInt);
            if (randomInt % 11 == 0) {
                log.warn("BOOK");
                context.getStateMachine().sendEvent(OrderEvent.PR_1_BOOK);
            }
        };
    }

    @Bean
    public Action<OrderState, OrderEvent> checkOrderedThings() {
        return context -> {
            final int randomInt = RANDOM.nextInt();
            log.info("check ordered things: {}", randomInt);
            if (randomInt % 7 == 0) {
                log.warn("COMPLETE");
                context.getStateMachine().sendEvent(OrderEvent.PR_2_COMPLETE);
            }
        };
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(4);
        return taskExecutor;
    }
}
