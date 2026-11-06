package ru.digitalhabits.support;

import io.cucumber.java.ParameterType;

import java.math.BigDecimal;

public class NumberParameterTypes {

    @ParameterType("[-+]?\\d+(?:\\.\\d+)?")
    public BigDecimal число(String value) {
        return new BigDecimal(value);
    }
}
