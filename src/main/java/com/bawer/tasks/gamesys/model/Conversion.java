package com.bawer.tasks.gamesys.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;


@Getter
@EqualsAndHashCode
public class Conversion {
    private final BigDecimal rate;
    private final Currency from;
    private final Currency to;
    private final RateChange rateChange;
    private final Instant time;

    public Conversion(BigDecimal rate, Currency from, Currency to, RateChange rateChange) {
        this(rate, from, to, rateChange, Instant.now().truncatedTo(ChronoUnit.MILLIS));
    }

    public Conversion(BigDecimal rate, Currency from, Currency to, RateChange rateChange, Instant time) {
        this.rate = rate;
        this.from = from;
        this.to = to;
        this.rateChange = rateChange;
        this.time = time;
    }
}
