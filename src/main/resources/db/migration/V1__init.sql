CREATE TABLE exchange_rate (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    currency_code VARCHAR(3) NOT NULL,
    rate          NUMERIC(20,10) NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_exchange_rate_currency_code ON exchange_rate (currency_code);

INSERT INTO exchange_rate (currency_code, rate) VALUES
    ('EUR', 1.0),
    ('USD', 1.1606),
    ('GBP', 0.8598),
    ('AUD', 1.639644),
    ('BRL', 6.03858),
    ('CAD', 1.6011),
    ('CHF', 0.9284),
    ('CNY', 8.2415),
    ('CZK', 23.902),
    ('DKK', 7.4643),
    ('HKD', 9.0255),
    ('HUF', 391.47),
    ('IDR', 19310.5),
    ('ILS', 3.8322),
    ('INR', 103.37),
    ('ISK', 149.3),
    ('JPY', 181.61),
    ('KRW', 1686.98),
    ('MXN', 21.0222),
    ('NOK', 11.7045),
    ('NZD', 1.965599),
    ('PLN', 4.2035),
    ('SEK', 11.0274),
    ('SGD', 1.5006),
    ('THB', 37.668),
    ('TRY', 48.9194),
    ('ZAR', 20.033);
