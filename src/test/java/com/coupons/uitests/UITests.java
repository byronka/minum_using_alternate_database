package com.coupons.uitests;

import com.coupons.Registry;
import com.coupons.database.DatabaseMigration;
import com.coupons.database.ExternalDatabase;
import com.coupons.database.SqlData;
import com.coupons.utils.CouponsContext;
import com.renomad.minum.logging.TestLogger;
import com.renomad.minum.state.Context;
import com.renomad.minum.web.FullSystem;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.sql.ResultSet;
import java.util.Optional;
import java.util.function.Function;

import static com.coupons.database.ExternalDatabase.makeNotNullable;
import static com.renomad.minum.testing.TestFramework.assertEquals;
import static com.renomad.minum.testing.TestFramework.buildTestingContext;

public class UITests {
    private static WebDriver driver;

    private static TestLogger logger;
    private static Context context;
    private static ExternalDatabase database;

    @BeforeClass
    public static void setUp() {
        context = buildTestingContext("_ui_test");
        FullSystem fullSystem = new FullSystem(context).start();
        var couponsContext = CouponsContext.build(fullSystem);
        new DatabaseMigration(couponsContext).run();
        new Registry(fullSystem, couponsContext).registerDomains();
        logger = (TestLogger) context.getLogger();
        database = couponsContext.database();

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }

    @AfterClass
    public static void tearDown() {
        driver.quit();

        context.getLogger().stop();
        context.getExecutorService().shutdownNow();
    }

    /**
     * Public user can submit a coupon code (*required)
     *  [ ] Website*
     *  [ ] Code*
     *  [ ] Discount*
     *  [ ] Type = $ or %*
     *  [ ] Description
     * Public receives message on screen,
     *     "Thank you for submitting your coupon code."
     * Coupon code submission is added to database in review status
     */
    @Test
    public void test_ShouldCreateCoupon() {
        // open the page
        driver.get("http://localhost:8080/");

        logger.test("The user can enter a website value for the coupon");
        driver.findElement(By.id("website_input")).sendKeys("http://foo.com");

        logger.test("The user can enter a coupon code");
        driver.findElement(By.id("code_input")).sendKeys("MYNAME10");

        logger.test("The user can enter a discount amount");
        driver.findElement(By.id("discount_amount_input")).sendKeys("5");

        logger.test("The user can pick for the discount to be a percentage");
        driver.findElement(By.id("discount_type_percentage")).click();

        logger.test("The user can enter an optional description");
        driver.findElement(By.id("description_input")).sendKeys("This is an optional description");

        logger.test("The user can submit the coupon");
        driver.findElement(By.id("coupon_submit")).click();

        final String confirmationMessage = driver.findElement(By.id("confirmation_message")).getText();
        String expectedConfirmationMessage = "Thank you for submitting your coupon code.";

        logger.test("The user receives an expected confirmation message");
        assertEquals(expectedConfirmationMessage, confirmationMessage);

        Function<ResultSet, Optional<String>> extractor =
                database.createExtractor(rs -> Optional.of(makeNotNullable(rs.getString(1))));

        var recentCouponStatus = database.runQuery(new SqlData<>(
                "get the status of recent coupon entry",
                "SELECT STATUS FROM COUPON",
                extractor));

        assertEquals(recentCouponStatus, Optional.of("reviewing"));
    }
}
