package com.epam.sm.example.config;


import com.epam.sm.example.squirrel.SquirrelStateMachineEntity;
import com.epam.sm.example.squirrel.SquirrelStateMachineRepository;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Profile("squirrel")
@Configuration
@EnableJpaRepositories(basePackageClasses = SquirrelStateMachineRepository.class)
@EntityScan(basePackageClasses = SquirrelStateMachineEntity.class)
public class SquirrelJpaConfig {
}
