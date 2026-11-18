package com.demo.exchangeservice.exchange.controller;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ConversionResponse(@NotNull BigDecimal convertedAmount) {}
