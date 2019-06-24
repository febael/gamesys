package com.bawer.tasks.gamesys.service;

import com.bawer.tasks.gamesys.model.Currency;
import com.bawer.tasks.gamesys.repository.jdbc.h2.ConversionRepository;
import org.junit.Test;
import org.mockito.Mockito;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConversionTaskIntegrationTests {

    private static final Map<String, Object> propertiesMap = new Yaml().load(
            ConversionTaskIntegrationTests.class.getClassLoader().getResourceAsStream("application.yml"));

    @Test
    public void givenAValidConversionTask_WhenItIsExecuted_ThenAnInsertToRepoShouldHappen() {
        // given
        var mockedRepo = Mockito.mock(ConversionRepository.class);
        when(mockedRepo.insert((any()))).thenReturn(true);
        var from = Currency.USD;
        var to = Currency.RUB;
        var applicationProperties = (Map<String, String>)propertiesMap.get("application");
        var endpointTemplate = applicationProperties.get("endpointTemplate");
        var apiKey = applicationProperties.get("application.apiKey");
        var endpoint = String.format(endpointTemplate, from, to, apiKey);
        //when
        var task = new ConversionTask(Currency.USD, Currency.RUB, endpoint, mockedRepo);
        task.execute();
        //then
        verify(mockedRepo).insert(any());
    }

}