package com.bawer.tasks.gamesys;

import com.bawer.tasks.gamesys.model.ApiError;
import com.bawer.tasks.gamesys.model.Currency;
import com.bawer.tasks.gamesys.repository.jdbc.h2.ConversionRepository;
import com.bawer.tasks.gamesys.service.ConversionTask;
import org.h2.jdbcx.JdbcConnectionPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
@Configuration
@ControllerAdvice
public class Application {

    @Value("application.apiKey")
    private String apiKey;

    @Value("application.endPointTemplate")
    private String endpointTemplate;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    DataSource h2ConnectionPool() {
        return JdbcConnectionPool.create("jdbc:h2:./gamesys.db", "sa", "sa");
    }

    @Bean
    @Autowired
    List<ConversionTask> conversionTaskList(ConversionRepository repository) {
        return Collections.singletonList(new ConversionTask(
                Currency.USD,
                Currency.RUB,
                String.format(endpointTemplate, Currency.USD, Currency.RUB, apiKey),
                repository));
    }

    @ExceptionHandler({ ConversionFailedException.class })
    public ResponseEntity<Object> handleConversionFailedException(ConversionFailedException ex) {
        return new ResponseEntity<>(
                new ApiError(HttpStatus.BAD_REQUEST, "Cannot access endpoint", ex.getLocalizedMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ SQLRuntimeException.class })
    public ResponseEntity<Object> handleSQLRuntimeException(SQLRuntimeException ex) {
        return new ResponseEntity<>(
                new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot process request", ex.getCause().getLocalizedMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
