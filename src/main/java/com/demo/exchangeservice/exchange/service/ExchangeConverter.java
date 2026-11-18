package com.demo.exchangeservice.exchange.service;

import com.demo.exchangeservice.exchange.repository.ExchangeRateRepository;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class ExchangeConverter {

  private static final int RESULT_SCALE = 2;
  private static final MathContext MATH_CONTEXT = new MathContext(34);
  private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

  private final ExchangeRateRepository exchangeRateRepository;

  public ExchangeConverter(ExchangeRateRepository exchangeRateRepository) {
    this.exchangeRateRepository = exchangeRateRepository;
  }

  public BigDecimal convert(Currency source, Currency target, BigDecimal amount) {
    BigDecimal sourceRate = rateOf(source);
    BigDecimal targetRate = rateOf(target);

    BigDecimal converted =
        amount.divide(sourceRate, MATH_CONTEXT).multiply(targetRate, MATH_CONTEXT);
    return converted.setScale(RESULT_SCALE, ROUNDING_MODE);
  }

  private BigDecimal rateOf(Currency currency) {
    return exchangeRateRepository
        .findByCurrency(currency)
        .orElseThrow(
            () -> new ExchangeException("No exchange rate available for currency " + currency))
        .getRate();
  }
}
