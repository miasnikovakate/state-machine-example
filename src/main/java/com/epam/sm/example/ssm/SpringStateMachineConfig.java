package com.epam.sm.example.ssm;

import com.epam.sm.example.model.DeliveryType;
import com.epam.sm.example.model.OrderEvent;
import com.epam.sm.example.model.OrderState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import static com.epam.sm.example.model.OrderEvent.*;
import static com.epam.sm.example.model.OrderState.*;

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
                .initial(CREATED)
                .states(EnumSet.allOf(OrderState.class))
                .choice(DELIVERY_CHOICE);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderState, OrderEvent> transitions) throws Exception {
        transitions
                .withExternal()
                .source(CREATED).target(SUBMITTED).event(SUBMIT)
                .and()
                .withExternal()
                .source(SUBMITTED).target(PAID).event(PAY)
                .and()
                .withExternal()
                .source(SUBMITTED).target(CANCELLED).event(CANCEL)
                .and()
                .withExternal()
                .source(PAID).target(PROCESSING)
                .and()
                .withExternal()
                .source(PROCESSING).target(DELIVERY_CHOICE).event(READY)
                .and()
                .withChoice()
                .source(DELIVERY_CHOICE)
                .first(MAIL_DELIVERY, isMailDeliveryType())
                .then(SERVICE_DELIVERY, isDeliveryServiceDeliveryType())
                .last(DELIVERY_TO_SHOP)
                .and()
                .withExternal()
                .source(MAIL_DELIVERY).target(DELIVERED).event(COMPLETE)
                .and()
                .withExternal()
                .source(SERVICE_DELIVERY).target(DELIVERED).event(COMPLETE)
                .and()
                .withExternal()
                .source(DELIVERY_TO_SHOP).target(DELIVERED).event(COMPLETE)
                .and()
                .withExternal()
                .source(DELIVERED).target(FULFILLED).event(FULFILL)
                .and()
                .withExternal()
                .source(DELIVERED).target(CANCELLED).event(CANCEL);
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

}
