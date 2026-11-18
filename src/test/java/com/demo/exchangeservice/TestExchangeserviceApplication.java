package com.demo.exchangeservice;

import org.springframework.boot.SpringApplication;

public class TestExchangeserviceApplication {

  public static void main(String[] args) {
    SpringApplication.from(ExchangeserviceApplication::main)
        .with(TestcontainersConfiguration.class)
        .run(args);
  }
}
