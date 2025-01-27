package com.coupons.utils;

import com.renomad.minum.logging.ILogger;
import com.renomad.minum.utils.StacktraceUtils;

import java.io.FileInputStream;
import java.util.Properties;

public class Constants {

    private Properties properties;
    private ILogger logger;

    public Constants(ILogger logger) {
        this.logger = logger;
        properties = getConfiguredProperties();

        TEMPLATE_DIRECTORY = properties.getProperty("TEMPLATE_DIRECTORY",  "templates");
        DATABASE_CONNECTION_STRING = properties.getProperty("DATABASE_CONNECTION_STRING");
        DATABASE_USERNAME = properties.getProperty("DATABASE_USERNAME");
        DATABASE_PASSWORD = properties.getProperty("DATABASE_PASSWORD");
    }

    private Properties getConfiguredProperties() {
        var myProps = new Properties();
        String fileName = "coupons.config";
        try (FileInputStream fis = new FileInputStream(fileName)) {
            logger.logDebug(() -> " found properties file at ./coupons.config.  Loading properties");
            myProps.load(fis);
        } catch (Exception ex) {
            logger.logDebug(() -> "Failed to successfully read coupons.config: " + StacktraceUtils.stackTraceToString(ex));
            System.exit(1);
        }
        return myProps;
    }

    /**
     * Where we store HTML templates
     */
    public final String TEMPLATE_DIRECTORY;

    /**
     * The full connection string to connect to our database
     */
    public final String DATABASE_CONNECTION_STRING;

    /**
     * The username for our database
     */
    public final String DATABASE_USERNAME;

    /**
     * The password for our database
     */
    public final String DATABASE_PASSWORD;

}
