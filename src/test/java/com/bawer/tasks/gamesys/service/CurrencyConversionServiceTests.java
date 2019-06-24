package com.bawer.tasks.gamesys.service;

import com.bawer.tasks.gamesys.model.Currency;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class CurrencyConversionServiceTests {

    @Test(expected = IllegalArgumentException.class)
    public void whenSameCurrencyIsGiven_ThenExceptionIsThrown() {
        var mock = Mockito.mock(CurrencyConversionService.class);
        when(mock.latest(Currency.USD, Currency.USD, 1)).thenCallRealMethod();
        mock.latest(Currency.USD, Currency.USD, 1);
    }
}