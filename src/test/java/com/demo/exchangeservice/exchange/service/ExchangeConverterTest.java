package com.demo.exchangeservice.exchange.service;

import com.demo.exchangeservice.exchange.repository.ExchangeRate;
import com.demo.exchangeservice.exchange.repository.ExchangeRateRepository;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeConverterTest {

    private static final BigDecimal EUR_RATE = new BigDecimal("1");
    private static final BigDecimal USD_RATE = new BigDecimal("1.1606");
    private static final BigDecimal AUD_RATE = new BigDecimal("1.639644");
    private static final BigDecimal BRL_RATE = new BigDecimal("6.03858");

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    private ExchangeConverter exchangeConverter;

    @BeforeEach
    void setUp() {
        exchangeConverter = new ExchangeConverter(exchangeRateRepository);
    }

    @Test
    void convertsEurToUsd() {
        // given
        givenRates(Currency.EUR, Currency.USD);

        // when
        BigDecimal result = exchangeConverter.convert(Currency.EUR, Currency.USD, new BigDecimal("123.45"));

        // then
        assertThat(result).isEqualByComparingTo("143.28");
    }

    @Test
    void convertsBetweenNonBaseCurrenciesViaEur() {
        // given
        givenRates(Currency.AUD, Currency.BRL);

        // when
        BigDecimal result = exchangeConverter.convert(Currency.AUD, Currency.BRL, new BigDecimal("77.77"));

        // then
        assertThat(result).isEqualByComparingTo("286.42");
    }

    @Test
    void convertsSameCurrencyWithoutChange() {
        // given
        givenRates(Currency.EUR);

        // when
        BigDecimal result = exchangeConverter.convert(Currency.EUR, Currency.EUR, new BigDecimal("123.45"));

        // then
        assertThat(result).isEqualByComparingTo("123.45");
    }

    @Test
    void roundsResultOnceToTwoDecimalsHalfUp() {
        // given
        givenRates(Currency.EUR, Currency.USD);

        // when
        BigDecimal eurToUsd = exchangeConverter.convert(Currency.EUR, Currency.USD, new BigDecimal("2.5555"));
        BigDecimal usdToEur = exchangeConverter.convert(Currency.USD, Currency.EUR, new BigDecimal("2.5555"));

        // then
        assertThat(eurToUsd).isEqualByComparingTo("2.97");
        assertThat(usdToEur).isEqualByComparingTo("2.20");
    }

    @Test
    void rejectsCurrencyWithoutStoredRate() {
        // given
        givenNoRatesStored();

        // when
        ThrowingCallable conversionCall = () -> exchangeConverter.convert(Currency.EUR, Currency.USD, BigDecimal.TEN);

        // then
        assertThatThrownBy(conversionCall)
                .isInstanceOf(ExchangeException.class)
                .hasMessageContaining("No exchange rate available");
    }

    private void givenRates(Currency... currencies) {
        for (Currency currency : currencies) {
            BigDecimal rate = switch (currency) {
                case EUR -> EUR_RATE;
                case USD -> USD_RATE;
                case AUD -> AUD_RATE;
                case BRL -> BRL_RATE;
                default -> throw new IllegalArgumentException("rate not defined in test: " + currency);
            };
            lenient().when(exchangeRateRepository.findByCurrency(currency))
                    .thenReturn(Optional.of(new ExchangeRate(currency, rate)));
        }
    }

    private void givenNoRatesStored() {
        when(exchangeRateRepository.findByCurrency(any())).thenReturn(Optional.empty());
    }
}
