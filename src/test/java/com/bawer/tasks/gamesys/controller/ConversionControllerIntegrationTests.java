package com.bawer.tasks.gamesys.controller;

import com.bawer.tasks.gamesys.Application;
import com.bawer.tasks.gamesys.model.Currency;
import com.bawer.tasks.gamesys.service.CurrencyConversionService;
import net.minidev.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.isA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class ConversionControllerIntegrationTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    CurrencyConversionService service;

    private static final String VALID_CURRENCY_1 = Currency.USD.name();
    private static final String VALID_CURRENCY_2 = Currency.RUB.name();
    private static final String INVALID_CURRENCY = "ZZZ";
    private static final String URI_STRING_TEMPLATE = "/api/conversions/%s2%s";

    @Test
    public void whenInvalidCurrencyIsRequested_ThenGetBadRequest() throws Exception {
        mvc.perform( get(String.format(URI_STRING_TEMPLATE, VALID_CURRENCY_1, INVALID_CURRENCY)) )
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void whenBothCurrenciesAreValid_ThenGetResult() throws Exception {
        when(service.latest(any(), any(), anyInt())).thenReturn(Collections.emptyList());
        mvc.perform( get(String.format(URI_STRING_TEMPLATE, VALID_CURRENCY_1, VALID_CURRENCY_2)) )
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$", isA(JSONArray.class)));
    }

}