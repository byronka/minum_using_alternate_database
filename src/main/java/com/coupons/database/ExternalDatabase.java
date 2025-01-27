package com.coupons.database;

import com.coupons.utils.Constants;
import org.h2.jdbcx.JdbcConnectionPool;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;
import java.util.function.Function;

/**
 * This code relates to databases that exist outside of our application.
 * For example, perhaps it refers to the H2 or Postgres databases.
 */
public class ExternalDatabase {

    private final DataSource dataSource;

    public ExternalDatabase(Constants constants) {
        this(obtainConnectionPool(constants));

    }

    ExternalDatabase(DataSource ds) {
        dataSource = ds;
    }

    private static JdbcConnectionPool obtainConnectionPool(Constants constants) {
        return JdbcConnectionPool.create(
                constants.DATABASE_CONNECTION_STRING,
                constants.DATABASE_USERNAME,
                constants.DATABASE_PASSWORD);
    }

    /*
     * ==========================================================
     * ==========================================================
     *
     *  Micro ORM
     *    a simplistic Object Relational Mapper (ORM)
     *    implementation.  These are the methods that comprise
     *    the mechanisms for that.
     *
     *    In comparison, a gargantuan project like Hibernate
     *    would consist of a heckuva-lot-more than this.  That's
     *    why this one is termed, "micro"
     *
     * ==========================================================
     * ==========================================================
     */

    public long executeInsertTemplate(
            String description,
            String preparedStatement,
            Object ... params) {
        final SqlData<Object> sqlData = new SqlData<>(description, preparedStatement, params);
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement st = prepareStatementWithKeys(sqlData, connection)) {
                return executeInsertOnPreparedStatement(sqlData, st);
            }
        } catch (SQLException ex) {
            throw new SqlRuntimeException(ex);
        }
    }


    <T> long executeInsertOnPreparedStatement(SqlData<T> sqlData, PreparedStatement st) throws SQLException {
        sqlData.applyParametersToPreparedStatement(st);
        st.executeUpdate();
        try (ResultSet generatedKeys = st.getGeneratedKeys()) {
            long newId;
            if (generatedKeys.next()) {
                newId = generatedKeys.getLong(1);
                assert (newId > 0);
            } else {
                throw new SqlRuntimeException("failed Sql.  Description: " + sqlData.description + " SQL code: " + sqlData.preparedStatement);
            }
            return newId;
        }
    }


    /**
     * A helper method.  Simply creates a prepared statement that
     * always returns the generated keys from the database, like
     * when you insert a new row of data in a table with auto-generating primary key.
     *
     * @param sqlData    see {@link SqlData}
     * @param connection a typical {@link Connection}
     */
    private <T> PreparedStatement prepareStatementWithKeys(SqlData<T> sqlData, Connection connection) throws SQLException {
        return connection.prepareStatement(
                sqlData.preparedStatement,
                Statement.RETURN_GENERATED_KEYS);
    }


    public <R> Optional<R> runQuery(SqlData<R> sqlData) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement st =
                         connection.prepareStatement(sqlData.preparedStatement)) {
                sqlData.applyParametersToPreparedStatement(st);
                try (ResultSet resultSet = st.executeQuery()) {
                    return sqlData.extractor.apply(resultSet);
                }
            }
        } catch (SQLException ex) {
            throw new SqlRuntimeException(ex);
        }

    }


    /**
     * This is an interface to a wrapper around {@link Function} so we can catch exceptions
     * in the generic function.
     *
     * @param <R> The return type
     * @param <E> The type of the exception
     */
    @FunctionalInterface
    public interface ThrowingFunction<R, E extends Exception> {
        R apply(ResultSet resultSet) throws E;
    }


    /**
     * This wraps the throwing function, so that we are not forced to
     * catch an exception in our ordinary code - it's caught and handled
     * here.
     * @param throwingFunction a lambda that throws a checked exception we have to handle.
     *                         specifically in this case that's a SqlRuntimeException
     * @param <R> the type of value returned
     * @return returns a function that runs and returns a function wrapped with an exception handler
     */
    static <R> Function<ResultSet, R> throwingFunctionWrapper(
            ThrowingFunction<R, Exception> throwingFunction) {

        return resultSet -> {
            try {
                return throwingFunction.apply(resultSet);
            } catch (Exception ex) {
                throw new SqlRuntimeException(ex);
            }
        };
    }


    /**
     * checks the String you pass in; if it's null, return an empty String.
     * Otherwise, return the unchanged string.
     */
    public static String makeNotNullable(String s) {
        return s == null ? "" : s;
    }


    /**
     * Accepts a function to extract data from a {@link ResultSet} and
     * removes some boilerplate with handling its response.
     * Works in conjunction with {@link #throwingFunctionWrapper}
     * @param extractorFunction a function that extracts data from a {@link ResultSet}
     * @param <T> the type of data we'll retrieve from the {@link ResultSet}
     * @return either the type of data wrapped with an optional, or {@link Optional#empty}
     */
    public <T> Function<ResultSet, Optional<T>> createExtractor(
            ThrowingFunction<Optional<T>, Exception> extractorFunction) {
        return throwingFunctionWrapper(rs -> {
            if (rs.next()) {
                return extractorFunction.apply(rs);
            } else {
                return Optional.empty();
            }
        });
    }


}
