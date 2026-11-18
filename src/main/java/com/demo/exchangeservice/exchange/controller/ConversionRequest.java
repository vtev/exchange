package com.demo.exchangeservice.exchange.controller;

import com.demo.exchangeservice.exchange.service.Currency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ConversionRequest(
        @NotNull Currency sourceCurrency,
        @NotNull Currency targetCurrency,
        @NotNull @Positive BigDecimal amount) {
}
