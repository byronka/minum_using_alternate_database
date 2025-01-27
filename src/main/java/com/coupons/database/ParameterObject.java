package com.coupons.database;

/**
 * a POJO container for the parameters for the {@link SqlData} object.
 */
public record ParameterObject<T>(
        /*
         * The data we are injecting into the SQL statement
         */
        Object data,

        /*
         * The type of the data we are injecting into the SQL statement (e.g. Integer, String, etc.)
         */
        Class<T> type
        ) {

}
