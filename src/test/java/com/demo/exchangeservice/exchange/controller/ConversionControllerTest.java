package com.demo.exchangeservice.exchange.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demo.exchangeservice.common.GlobalExceptionHandler;
import com.demo.exchangeservice.exchange.service.Currency;
import com.demo.exchangeservice.exchange.service.ExchangeConverter;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ConversionController.class)
@Import({
  GlobalExceptionHandler.class,
  JacksonAutoConfiguration.class,
  ValidationAutoConfiguration.class
})
class ConversionControllerTest {

  private static final String CONVERT_URL = "/api/v1/convert";

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ExchangeConverter exchangeConverter;

  @Test
  void returnsConvertedAmount() throws Exception {
    // given
    givenConverterReturns("116.06");

    // when
    var response =
        mockMvc.perform(
            post(CONVERT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(conversionRequest("EUR", "USD", "100")));

    // then
    response
        .andExpect(status().isOk())
        .andExpect(
            content()
                .json(
                    """
                    {"convertedAmount": 116.06}
                    """));
  }

  @Test
  void rejectsNonPositiveAmount() throws Exception {
    // given

    // when
    var response =
        mockMvc.perform(
            post(CONVERT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(conversionRequest("EUR", "USD", "0")));

    // then
    response
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(jsonPath("$.detail").value("Request validation failed: amount"))
        .andExpect(jsonPath("$.fields.amount").value("must be greater than 0"));
  }

  @Test
  void rejectsNegativeAmount() throws Exception {
    // given

    // when
    var response =
        mockMvc.perform(
            post(CONVERT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(conversionRequest("EUR", "USD", "-5")));

    // then
    response
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.fields.amount").value("must be greater than 0"));
  }

  @Test
  void rejectsMissingAmount() throws Exception {
    // given

    // when
    var response =
        mockMvc.perform(
            post(CONVERT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"sourceCurrency": "EUR", "targetCurrency": "USD"}
                    """));

    // then
    response
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(jsonPath("$.detail").value("Request validation failed: amount"))
        .andExpect(jsonPath("$.fields.amount").value("must not be null"));
  }

  @Test
  void reportsAllInvalidFieldsAtOnce() throws Exception {
    // given

    // when
    var response =
        mockMvc.perform(post(CONVERT_URL).contentType(MediaType.APPLICATION_JSON).content("{}"));

    // then
    response
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(
            jsonPath("$.detail")
                .value("Request validation failed: amount, sourceCurrency, targetCurrency"))
        .andExpect(jsonPath("$.fields.sourceCurrency").value("must not be null"))
        .andExpect(jsonPath("$.fields.targetCurrency").value("must not be null"))
        .andExpect(jsonPath("$.fields.amount").value("must not be null"));
  }

  @Test
  void rejectsUnknownCurrencyCode() throws Exception {
    // given

    // when
    var response =
        mockMvc.perform(
            post(CONVERT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(conversionRequest("EUR", "XXX", "100")));

    // then
    response
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(
            jsonPath("$.detail")
                .value(
                    "Unsupported Currency: XXX. Supported values: " + Currency.supportedValues()));
  }

  @Test
  void rejectsLowercaseCurrencyCodes() throws Exception {
    // given

    // when
    var response =
        mockMvc.perform(
            post(CONVERT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(conversionRequest("eur", "usd", "100")));

    // then
    response
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(
            jsonPath("$.detail")
                .value(org.hamcrest.Matchers.containsString("Unsupported Currency: eur")));
  }

  @Test
  void rejectsMalformedJson() throws Exception {
    // given

    // when
    var response =
        mockMvc.perform(
            post(CONVERT_URL).contentType(MediaType.APPLICATION_JSON).content("not json"));

    // then
    response.andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").value("Bad Request"));
  }

  private static String conversionRequest(String source, String target, String amount) {
    return """
    {"sourceCurrency": "%s", "targetCurrency": "%s", "amount": %s}
    """
        .formatted(source, target, amount);
  }

  private void givenConverterReturns(String convertedAmount) {
    when(exchangeConverter.convert(any(), any(), any()))
        .thenReturn(new BigDecimal(convertedAmount));
  }
}
