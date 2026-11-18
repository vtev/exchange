package com.demo.exchangeservice.exchange.controller;

import com.demo.exchangeservice.exchange.service.ExchangeConverter;
import java.math.BigDecimal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ConversionController implements ConversionApi {

  private final ExchangeConverter exchangeConverter;

  public ConversionController(ExchangeConverter exchangeConverter) {
    this.exchangeConverter = exchangeConverter;
  }

  @Override
  @PostMapping("/convert")
  public ConversionResponse convert(@RequestBody @Validated ConversionRequest request) {
    BigDecimal convertedAmount =
        exchangeConverter.convert(
            request.sourceCurrency(), request.targetCurrency(), request.amount());
    return new ConversionResponse(convertedAmount);
  }
}
