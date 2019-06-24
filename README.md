# Gamesys Task
Write a simple restful service, which reqularly reads entries from an http source and returns last 10 entries on demand.

For the purpose, regular checks to an online currency conversion service is made every minute.

## About Design
* Used Java11
* Made use of these frameworks/libraries/tools :
     * __Gradle 5__ => build automation
     * __Spring Boot 2__ => auto-configuration, dependency management, dependency injection, embedded container
     * __Logback__ => logging
     * __H2__ => SQL data storage in Embedded&Persisted mode
     * __JUnit 4__ => testing project
* Designed endpoint structure is as follows (ApiResponse is a common model for responses holding returnValue or error definition) :
```
(Get last 10)      GET     /api/conversions/{CURFROM}2{CURTO}   ,[Conversion]
```
__CURFROM__ and __CURTO__ are path parameters of 3 capital letters that are supposed to be converted to internal enum class Currency.
* Currently only USD2RUB rates are grabbed every 1 minute, however multiple tasks can be added.
* Regular http requests are made to __www.alphavantage.co__. Enrichment on the returned data is kept very simple, compared to previous rate, a field is set to ZZZ (rather steady), HOP (increasing) or BOM (falling)
* DB is very simple with a single table for Conversions. 

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. 

### Prerequisites
* You need to have JRE (or JDK) 11+ installed on your machine. 
* You also need internet, so that gradle can download dependencies. 
* Gradle will be installed with gradlew. 

## Running The Tests And Building
A call to the gradle task __test__ will run all tests on the terminal 
```
./gradlew test
```
You may also want to import gradle project into your favorite IDE and run tests on it.

Sample request with cURL :
```
echo "Sending a valid request" && curl -XPOST 127.0.0.1:8080/conversions/USD2RUB -H "Content-Type: application/json" -i
```

Current test methods are : 
* test1__GivenTheFirstConversionToInsert_WhenIInsertIt_IShouldBeAbleToReadItBackAndOnlyIt
* test2__WhenIInsertFirstConversionAgain_IShouldGetException
* test3__WhenIDoManyInserts_ThenIShouldBeAbleToReadThemCorrectly
* whenBothCurrenciesAreValid_ThenGetResult
* whenInvalidCurrencyIsRequested_ThenGetBadRequest
* givenAValidConversionTask_WhenItIsExecuted_ThenAnInsertToRepoShouldHappen
* whenSameCurrencyIsGiven_ThenExceptionIsThrown