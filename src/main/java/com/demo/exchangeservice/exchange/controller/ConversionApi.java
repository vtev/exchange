package com.demo.exchangeservice.exchange.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;

@Tag(name = "Exchange", description = "Currency conversion")
public interface ConversionApi {

    @Operation(
            summary = "Convert an amount between currencies",
            description = "Converts a monetary amount from one currency to another using the stored "
                    + "EUR-based exchange rates (each rate is the price of 1 EUR in that currency). "
                    + "The result is rounded to 2 decimal places (HALF_UP).")
    @ApiResponse(
            responseCode = "200",
            description = "Converted amount",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ConversionResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request (non-positive amount or unsupported currency)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    ConversionResponse convert(
            @RequestBody(
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ConversionRequest.class)))
            ConversionRequest request);
}
