package com.coupons.utils;

import java.io.IOException;

public final class FileUtils {

    private final com.renomad.minum.utils.FileUtils minumFileUtils;
    private final Constants constants;

    public FileUtils(com.renomad.minum.utils.FileUtils minumFileUtils, Constants constants) {
        this.minumFileUtils = minumFileUtils;
        this.constants = constants;
    }

    /**
     * Read a template file, expected to use this with {@link com.renomad.minum.templating.TemplateProcessor}
     */
    public String readTemplate(String path) {
        try {
            return minumFileUtils.readTextFile(constants.TEMPLATE_DIRECTORY + path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
