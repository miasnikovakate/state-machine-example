package com.epam.sm.example.akka;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Getter
@Builder
public class OrderData implements AkkaData {
    private double totalSum;
}
