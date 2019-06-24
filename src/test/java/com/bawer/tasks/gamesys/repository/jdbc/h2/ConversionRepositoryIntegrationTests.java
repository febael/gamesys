package com.bawer.tasks.gamesys.repository.jdbc.h2;

import com.bawer.tasks.gamesys.SQLRuntimeException;
import com.bawer.tasks.gamesys.model.Conversion;
import com.bawer.tasks.gamesys.model.Currency;
import com.bawer.tasks.gamesys.model.RateChange;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConversionRepositoryIntegrationTests {

    private static ConversionRepository repository;

    private static Conversion firstConversion;

    @BeforeClass
    public static void initializeDB() throws Exception {
        var dataSource = JdbcConnectionPool.create("jdbc:h2:./gamesystest.db", "sa", "sa");
        var tableNameField = ConversionRepository.class.getDeclaredField("TABLE_NAME");
        tableNameField.setAccessible(true);
        var tableName = (String) tableNameField.get(null);
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement("drop table "+tableName)
        ) {
            stmt.execute();
        } catch (SQLException e) { /* swallow */ }
        repository = new ConversionRepository(dataSource);
        var checkTableExistenceMethod = ConversionRepository.class.getDeclaredMethod("checkTableExistence");
        checkTableExistenceMethod.setAccessible(true);
        checkTableExistenceMethod.invoke(repository);
    }

    @Test
    public void test1__GivenTheFirstConversionToInsert_WhenIInsertIt_IShouldBeAbleToReadItBackAndOnlyIt() {
        firstConversion = createConversion();
        assertTrue( repository.insert(firstConversion) );
        var list = repository.getLatest(firstConversion.getFrom(), firstConversion.getTo(), 10);
        assertEquals(1, list.size());
        assertEquals(firstConversion, list.get(0));
    }

    @Test(expected = SQLRuntimeException.class)
    public void test2__WhenIInsertFirstConversionAgain_IShouldGetException() {
        repository.insert(firstConversion);
    }

    /* Produce enough conversions to make at least group, overflow count 10.
    * so produce 1 + (noOfCurrencies*(noOfCurrencies-1)*10)
    */
    @Test
    public void test3__WhenIDoManyInserts_ThenIShouldBeAbleToReadThemCorrectly() throws Exception {
        initializeDB();
        var loopSize = 1 + (Currency.values().length * (Currency.values().length - 1) * 10);
        var allConversions = new LinkedList<Conversion>();
        do {
            var conversion = createConversion();
            assertTrue( repository.insert(conversion) );
            allConversions.add(conversion);
            Thread.sleep(2); // for guaranteeing unique index
        } while (--loopSize > 0);
        var allConversionsGrouped = allConversions.stream().collect(
                Collectors.groupingBy(c -> new fromToPair(c.getFrom(), c.getTo())));
        for (var conversionsList : allConversionsGrouped.values()) {
            conversionsList.sort(Comparator.comparing(Conversion::getTime));
            var listFromDB = repository.getLatest(conversionsList.get(0).getFrom(), conversionsList.get(1).getTo(), 10);
            assertTrue(conversionsList.size() >= listFromDB.size());
            var index = 0;
            for (var conversion : conversionsList) {
                assertEquals(conversion, conversionsList.get(index++));
            }
        }
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    private class fromToPair {
        private final Currency from;
        private final Currency to;
    }

    private Conversion createConversion() {
        var random = new Random();
        var from = Currency.values()[random.nextInt(Currency.values().length)];
        Currency to;
        do {
            to = Currency.values()[random.nextInt(Currency.values().length)];
        } while (from == to);
        var rateChange = RateChange.values()[random.nextInt(RateChange.values().length)];
        var rate = new BigDecimal(random.nextDouble()).setScale(4, RoundingMode.HALF_UP);
        return new Conversion(rate, from, to, rateChange);
    }
}