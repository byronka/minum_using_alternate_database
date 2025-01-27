package com.coupons;

import com.coupons.utils.CouponsContext;
import com.renomad.minum.htmlparsing.HtmlParseNode;
import com.renomad.minum.htmlparsing.TagName;
import com.renomad.minum.logging.TestLogger;
import com.renomad.minum.state.Context;
import com.renomad.minum.web.FullSystem;
import com.renomad.minum.web.FunctionalTesting;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.renomad.minum.testing.TestFramework.*;
import static com.renomad.minum.web.StatusLine.StatusCode.CODE_200_OK;


public class AppTest
{

    private static TestLogger logger;
    private static Context context;
    private static FunctionalTesting ft;

    @BeforeClass
    public static void init() {
        context = buildTestingContext("_integration_test");
        FullSystem fullSystem = new FullSystem(context).start();
        var couponsContext = CouponsContext.build(fullSystem);
        new Registry(fullSystem, couponsContext).registerDomains();
        logger = (TestLogger) context.getLogger();
        ft = new FunctionalTesting(context, "localhost", 8080);
    }

    @AfterClass
    public static void cleanup() {
        context.getLogger().stop();
        context.getExecutorService().shutdownNow();
    }


    @Test
    public void testFullSystem() throws IOException {
        /*
         A user should be able to enter some basic data for a coupon.

         Website*:     [____________]
         Code*:        [____________]
         Discount*:    [____________]
         Type*:        [____________]
         Description:  [____________]

         */
        logger.test("We should see certain inputs on the coupon entry page");
        var testResponse = ft.get("");
        assertEquals(testResponse.statusLine().status(), CODE_200_OK);
        List<HtmlParseNode> inputsOnPage = testResponse.search(TagName.LABEL, Map.of());
        assertEquals(testResponse.searchOne(TagName.LEGEND, Map.of()).innerText(), "Type*");

        // check there are labels as we expect
        var fh = new FinderHelper(inputsOnPage);
        assertTrue(fh.isFoundOnPage("Website*"));
        assertTrue(fh.isFoundOnPage("Code*"));
        assertTrue(fh.isFoundOnPage("Discount*"));
        assertTrue(fh.isFoundOnPage("Description"));
    }

    /**
     * Just a one-off helper class to reduce some duplication
     * in a test
     */
    static class FinderHelper {
        private final List<HtmlParseNode> nodes;

        public FinderHelper(List<HtmlParseNode> nodes) {
            this.nodes = nodes;
        }

        private boolean isFoundOnPage(String value) {
            return nodes.stream().anyMatch(x -> x.innerText().equals(value));
        }
    }



}
