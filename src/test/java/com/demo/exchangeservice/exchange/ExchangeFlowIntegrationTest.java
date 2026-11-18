package com.demo.exchangeservice.exchange;

import static org.assertj.core.api.Assertions.assertThat;

import com.demo.exchangeservice.TestcontainersConfiguration;
import com.demo.exchangeservice.exchange.controller.ConversionRequest;
import com.demo.exchangeservice.exchange.controller.ConversionResponse;
import com.demo.exchangeservice.exchange.service.Currency;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import(TestcontainersConfiguration.class)
class ExchangeFlowIntegrationTest {

  private static final String CONVERT_URL = "/api/v1/convert";

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void convertsEurToUsdEndToEnd() {
    // given
    ConversionRequest request =
        new ConversionRequest(Currency.EUR, Currency.USD, new BigDecimal("123.45"));

    // when
    ResponseEntity<ConversionResponse> response =
        restTemplate.postForEntity(CONVERT_URL, request, ConversionResponse.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().convertedAmount()).isEqualByComparingTo("143.28");
  }
}
