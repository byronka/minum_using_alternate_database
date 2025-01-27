package com.coupons.domain.coupons;

import com.coupons.database.ExternalDatabase;
import com.coupons.utils.CouponsContext;
import com.coupons.utils.FileUtils;
import com.renomad.minum.web.IRequest;
import com.renomad.minum.web.IResponse;
import com.renomad.minum.web.Response;

public class CouponEntry {

    private final String nameEntryTemplate;
    private final ExternalDatabase externalDatabase;

    public CouponEntry(CouponsContext couponsContext) {
        FileUtils fileUtils = couponsContext.fileUtils();
        nameEntryTemplate = fileUtils.readTemplate("coupons/coupon_entry.html");
        externalDatabase = couponsContext.database();
    }

    /**
     * This handles a GET request, showing the input fields
     * for creating a new coupon.
     */
    public IResponse getCouponEntry(IRequest request) {
        return Response.htmlOk(nameEntryTemplate);
    }

    /**
     * This receives a POST request, containing the data for a new coupon
     */
    public IResponse postCouponEntry(IRequest request) {
        var website = request.getBody().asString("website");
        var code = request.getBody().asString("code");
        var discount = request.getBody().asString("discount");
        var discountType = request.getBody().asString("discount_type");
        var description = request.getBody().asString("description");

        externalDatabase.executeInsertTemplate(
                "add a new coupon",
                """
                INSERT INTO COUPON
                       (website, code, discount, discount_type, description, status)
                VALUES (   ?   ,  ?  ,    ?    ,     ?        ,      ?     , 'reviewing');
                """,
                website, code, discount, discountType, description
        );

        return Response.htmlOk("""
                <p id="confirmation_message">
                Thank you for submitting your coupon code.
                </p>
                <p>
                <a href="/">Return</a>
                </p>
                """);
    }
}
