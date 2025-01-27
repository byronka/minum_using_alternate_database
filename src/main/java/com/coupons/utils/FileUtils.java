package com.coupons.utils;

public final class FileUtils {

    private final com.renomad.minum.utils.FileUtils minumFileUtils;
    private final Constants constants;

    public FileUtils(com.renomad.minum.utils.FileUtils minumFileUtils, Constants constants) {
        this.minumFileUtils = minumFileUtils;
        this.constants = constants;
    }

    /**
     * Read a template file, expected to use this with {@link minum.templating.TemplateProcessor}
     */
    public String readTemplate(String path) {
        return minumFileUtils.readTextFile(constants.TEMPLATE_DIRECTORY + path);
    }

}
