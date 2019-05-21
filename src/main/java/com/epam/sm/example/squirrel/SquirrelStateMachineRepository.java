package com.epam.sm.example.squirrel;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

@Profile("squirrel")
public interface SquirrelStateMachineRepository extends JpaRepository<SquirrelStateMachineEntity, String> {
}
