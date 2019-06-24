package com.bawer.tasks.gamesys.service;

import com.bawer.tasks.gamesys.model.Conversion;
import com.bawer.tasks.gamesys.model.Currency;
import com.bawer.tasks.gamesys.repository.jdbc.h2.ConversionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.List;

@Service
public class CurrencyConversionService {

    private final ConversionRepository repository;
    private final ScraperJob scraperJob;

    @Autowired
    public CurrencyConversionService(ScraperJob scraperJob, ConversionRepository repository) {
        this.repository = repository;
        this.scraperJob = scraperJob;
        scraperJob.start();
    }

    @PreDestroy
    private void endScraperJob() {
        if (scraperJob != null ) {
            scraperJob.end();
        }
    }

    public List<Conversion> latest(Currency from, Currency to, int count) {
        if (from == to) {
            throw new IllegalArgumentException("have you ever been converted to yourself?");
        }
        return repository.getLatest(from, to, count);
    }
}
