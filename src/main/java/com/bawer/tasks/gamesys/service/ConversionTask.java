package com.bawer.tasks.gamesys.service;

import com.bawer.tasks.gamesys.model.Conversion;
import com.bawer.tasks.gamesys.model.Currency;
import com.bawer.tasks.gamesys.model.RateChange;
import com.bawer.tasks.gamesys.repository.jdbc.h2.ConversionRepository;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@ToString
public class ConversionTask {

    private static final Logger logger = LoggerFactory.getLogger(ConversionTask.class);

    private static final Pattern EXCHANGE_RATE_PATTERN = Pattern.compile("Exchange Rate\": \"(0|(?>[1-9][0-9]*)\\.[0-9]{2,4})[0-9]*\"");

    private final Currency from;
    private final Currency to;
    private final String endpoint;
    private final ConversionRepository repository;

    private BigDecimal latestRate = BigDecimal.ZERO;
    private final BigDecimal HIGH_THRESHOLD = new BigDecimal(1.005);
    private final BigDecimal LOW_THRESHOLD = new BigDecimal(0.995);

    boolean execute() {
        try {
            HttpsURLConnection c = (HttpsURLConnection) new URL(endpoint).openConnection();
            c.setConnectTimeout(2000);
            c.setReadTimeout(3000);
            logger.debug("Updating task : {}", this);
            c.connect();
            int responseCode = c.getResponseCode();
            if (responseCode == 200) {
                var newRateString = getRateFromResponse(c.getInputStream());
                logger.debug("New rate : {}", newRateString);

                var newRate = new BigDecimal(newRateString);
                var rateChange = calculateRateChange(latestRate, newRate);
                var result = repository.insert(new Conversion(newRate, from, to, rateChange));
                if (!result) {
                    logger.warn("Couldn't insert rate into db.");
                }
                latestRate = newRate;
                return result;
            } else {
                String responseString = convertResponseToString(c.getErrorStream());
                logger.warn("Unexpected status code : "+responseCode+", response is : "+responseString);
                return false;
            }
        } catch (MalformedURLException e) {
            return false;
        } catch (IOException e) {
            logger.error("Got IOException, will retry", e);
            return true; // may be recoverable
        } catch (Exception e) {
            logger.error("Got Exception, will retry", e);
            return true;
        }
    }

    private RateChange calculateRateChange(BigDecimal latestRate, BigDecimal newRate) {
        if (Objects.equals(latestRate, BigDecimal.ZERO)) {
            return RateChange.ZZZ;
        }
        var change = newRate.divide(latestRate, MathContext.DECIMAL32);
        if (change.compareTo(HIGH_THRESHOLD) > 0) {
            return RateChange.HOP;
        }
        return change.compareTo(LOW_THRESHOLD) < 0 ? RateChange.BOM : RateChange.ZZZ;
    }

    private String getRateFromResponse(InputStream inputStream) throws Exception {
        String responseString = convertResponseToString(inputStream);
        Matcher m = EXCHANGE_RATE_PATTERN.matcher(responseString);
        if (! m.find() ) {
            throw new Exception("Cannot match ->"+ EXCHANGE_RATE_PATTERN+"<- in the "+responseString);
        }
        return m.group(1);
    }

    private String convertResponseToString(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String inputLine;
        var content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content.toString();
    }
}
