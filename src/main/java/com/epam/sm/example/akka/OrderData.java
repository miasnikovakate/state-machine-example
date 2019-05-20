package com.epam.sm.example.akka;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@Builder
public class OrderData implements AkkaData {
    private double totalSum;
}
