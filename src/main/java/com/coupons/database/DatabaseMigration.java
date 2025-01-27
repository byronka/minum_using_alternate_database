package com.coupons.database;

import com.coupons.utils.Constants;
import com.coupons.utils.CouponsContext;
import org.flywaydb.core.Flyway;

public class DatabaseMigration {

    private final Constants constants;

    public DatabaseMigration(CouponsContext couponsContext) {
        constants = couponsContext.constants();
    }
    /**
     * Kick off the database migration
     */
    public void run() {
        String url = constants.DATABASE_CONNECTION_STRING;
        String user = constants.DATABASE_USERNAME;
        String password = constants.DATABASE_PASSWORD;

        Flyway flyway = Flyway.configure().dataSource(url, user, password).load();
        flyway.migrate();
    }
}
