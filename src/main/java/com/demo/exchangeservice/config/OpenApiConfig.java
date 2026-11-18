package com.demo.exchangeservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(info = @Info(
        title = "Exchange Service",
        description = "REST API that converts monetary amounts between currencies using EUR-based exchange rates."))
public class OpenApiConfig {
}
