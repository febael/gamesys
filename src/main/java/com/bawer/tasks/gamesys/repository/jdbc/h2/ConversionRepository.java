package com.bawer.tasks.gamesys.repository.jdbc.h2;

import com.bawer.tasks.gamesys.SQLRuntimeException;
import com.bawer.tasks.gamesys.model.Conversion;
import com.bawer.tasks.gamesys.model.Currency;
import com.bawer.tasks.gamesys.model.RateChange;
import com.bawer.tasks.gamesys.repository.BaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Repository
public class ConversionRepository implements BaseRepository<Conversion> {

    private static final Logger logger = LoggerFactory.getLogger(ConversionRepository.class);

    private static final String TABLE_NAME = "conversion";
    private static final String CREATE_TABLE_STATEMENT = "CREATE TABLE "+TABLE_NAME+" (" +
            " from_cur CHAR(3)," +
            " to_cur CHAR(3)," +
            " time TIMESTAMP(4)," +
            " rate DECIMAL(20, 4)," +
            " rate_change CHAR(3)," +
            " PRIMARY KEY (from_cur, to_cur, time)" +
            ")";
    private static final String GET_LATEST_QUERY_TEMPLATE = "SELECT TOP %d time, rate, rate_change FROM "+TABLE_NAME+
            " WHERE from_cur = ? and to_cur = ?" +
            " ORDER BY time DESC";
    private static final String INSERT_QUERY_TEMPLATE = "INSERT INTO "+TABLE_NAME+" (from_cur, to_cur, time, rate, rate_change)" +
            " VALUES (?, ?, ?, ?, ?)";

    private final DataSource dataSource;

    @Autowired
    public ConversionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    private void checkTableExistence() {
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(CREATE_TABLE_STATEMENT)
        ) {
            stmt.execute();
            logger.info("Table '"+TABLE_NAME+"' is created");
        } catch (SQLException e) { /* swallow */ }
    }


    public List<Conversion> getLatest(Currency from, Currency to, int count) {
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement( String.format(GET_LATEST_QUERY_TEMPLATE, count) )
        ) {
            stmt.setString(1, from.name());
            stmt.setString(2, to.name());
            ResultSet rs = stmt.executeQuery();
            if (! rs.next() ) {
                return Collections.emptyList();
            }
            List<Conversion> listToReturn = new LinkedList<>();
            do {
                listToReturn.add(new Conversion(
                        rs.getBigDecimal(2),
                        from,
                        to,
                        RateChange.valueOf(rs.getString(3)),
                        rs.getObject(1, Instant.class)
                ));
            } while (rs.next());
            return listToReturn;
        } catch (SQLException e) {
            logger.error("Error during sql operation : ", e);
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public boolean insert(Conversion obj) {
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(INSERT_QUERY_TEMPLATE)
        ) {
            stmt.setString(1, obj.getFrom().name());
            stmt.setString(2, obj.getTo().name());
            stmt.setTimestamp(3, Timestamp.from(obj.getTime()));
            stmt.setBigDecimal(4, obj.getRate());
            stmt.setString(5, obj.getRateChange().name());
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            logger.error("Error during sql operation : ", e);
            throw new SQLRuntimeException(e);
        }
    }
}