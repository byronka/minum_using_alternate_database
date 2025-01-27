package com.coupons.utils;

import com.coupons.database.ExternalDatabase;
import com.renomad.minum.logging.ILogger;
import com.renomad.minum.web.FullSystem;

/**
 * An object we pass around throughout Coupons, holds instances of some
 * values we need throughout.
 * @param constants constant values that are set in coupons.config
 * @param fileUtils a wrapper around Minum's fileutils, custom for our needs
 * @param database holds an instance of a class that lets us access our database
 */
public record CouponsContext(Constants constants, FileUtils fileUtils, ExternalDatabase database) {

    public static CouponsContext build(FullSystem fs) {
        ILogger logger = fs.getContext().getLogger();
        Constants constants = new Constants(logger);
        com.renomad.minum.state.Constants minumConstants = fs.getContext().getConstants();
        var fileUtils = new FileUtils(new com.renomad.minum.utils.FileUtils(logger, minumConstants), constants);
        var database = new ExternalDatabase(constants);
        return new CouponsContext(constants, fileUtils, database);
    }
}
