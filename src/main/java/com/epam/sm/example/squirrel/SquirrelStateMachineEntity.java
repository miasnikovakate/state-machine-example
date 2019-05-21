package com.epam.sm.example.squirrel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SquirrelStateMachineEntity {
    @Id
    private String id;

    @Column(columnDefinition="text", nullable = false)
    private String data;
}
