package com.demo.exchangeservice.exchange.service;

import java.util.Arrays;

public enum Currency {

    EUR, USD, GBP, AUD, BRL, CAD, CHF, CNY, CZK, DKK,
    HKD, HUF, IDR, ILS, INR, ISK, JPY, KRW, MXN, NOK,
    NZD, PLN, SEK, SGD, THB, TRY, ZAR;

    public static String supportedValues() {
        return String.join(", ", Arrays.stream(values()).map(Enum::name).toList());
    }
}
