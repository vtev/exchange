package com.demo.exchangeservice.exchange.repository;

import com.demo.exchangeservice.exchange.service.Currency;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "exchange_rate")
public class ExchangeRate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "currency_code", nullable = false, length = 3)
  private Currency currency;

  @Column(name = "rate", nullable = false, precision = 20, scale = 10)
  private BigDecimal rate;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private Instant createdAt;

  private ExchangeRate() {}

  public ExchangeRate(Currency currency, BigDecimal rate) {
    this.currency = currency;
    this.rate = rate;
  }

  public Long getId() {
    return id;
  }

  public Currency getCurrency() {
    return currency;
  }

  public BigDecimal getRate() {
    return rate;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
