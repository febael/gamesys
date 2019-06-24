package com.bawer.tasks.gamesys.controller;

import com.bawer.tasks.gamesys.model.Conversion;
import com.bawer.tasks.gamesys.model.Currency;
import com.bawer.tasks.gamesys.service.CurrencyConversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/conversions")
public class ConversionController {

    @Autowired
    private CurrencyConversionService service;

    @RequestMapping(path = "/{from:[A-Z]{3}}2{to:[A-Z]{3}}", method = RequestMethod.GET)
    public ResponseEntity<List<Conversion>> last10(@PathVariable Currency from, @PathVariable Currency to) {
        var listOfConversions = service.latest(from, to, 10);
        return new ResponseEntity<>(listOfConversions, HttpStatus.OK);
    }
}
