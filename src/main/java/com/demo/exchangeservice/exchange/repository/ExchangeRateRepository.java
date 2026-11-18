package com.demo.exchangeservice.exchange.repository;

import com.demo.exchangeservice.exchange.service.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findByCurrency(Currency currency);
}
