package com.epam.sm.example.ssm;

import com.epam.sm.example.model.DeliveryType;
import com.epam.sm.example.model.OrderEvent;
import com.epam.sm.example.model.OrderState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Slf4j
@Configuration
@EnableStateMachineFactory
public class SpringStateMachineConfig extends EnumStateMachineConfigurerAdapter<OrderState, OrderEvent> {
    public static final String DELIVERY_TYPE = "DELIVERY_TYPE";

    @Override
    public void configure(StateMachineConfigurationConfigurer<OrderState, OrderEvent> config) throws Exception {
        config
                .withConfiguration()
                .autoStartup(true)
                .listener(logListener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<OrderState, OrderEvent> states) throws Exception {
        states
                .withStates()
                .initial(OrderState.CREATED)
                .states(EnumSet.allOf(OrderState.class))
                .choice(OrderState.DELIVERY_CHOICE);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderState, OrderEvent> transitions) throws Exception {
        transitions.withExternal()
                .source(OrderState.CREATED).target(OrderState.SUBMITTED).event(OrderEvent.SUBMIT).action(submitAction())
                .and()
                .withExternal()
                .source(OrderState.SUBMITTED).target(OrderState.PAID).event(OrderEvent.PAY)
                .and()
                .withExternal()
                .source(OrderState.SUBMITTED).target(OrderState.CANCELLED).event(OrderEvent.CANCEL)
                .and()
                .withExternal()
                .source(OrderState.PAID).target(OrderState.PROCESSING)
                .and()
                .withExternal()
                .source(OrderState.PROCESSING).target(OrderState.DELIVERY_CHOICE).event(OrderEvent.READY)
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
        };
    }

    @Bean
    public Action<OrderState, OrderEvent> submitAction() {
        return context -> {
        };
    }

}
