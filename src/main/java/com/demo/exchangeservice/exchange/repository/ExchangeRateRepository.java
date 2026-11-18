package com.demo.exchangeservice.exchange.repository;

import com.demo.exchangeservice.exchange.service.Currency;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

  Optional<ExchangeRate> findByCurrency(Currency currency);
}
