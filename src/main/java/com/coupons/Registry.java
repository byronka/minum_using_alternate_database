package com.coupons;

import com.coupons.domain.coupons.CouponEntry;
import com.coupons.utils.CouponsContext;
import com.renomad.minum.state.Context;
import com.renomad.minum.web.FullSystem;
import com.renomad.minum.web.WebFramework;

import static com.renomad.minum.web.RequestLine.Method.GET;
import static com.renomad.minum.web.RequestLine.Method.POST;

public class Registry {

    private final WebFramework webFramework;
    private final Context context;
    private final CouponsContext couponsContext;

    public Registry(FullSystem fs, CouponsContext couponsContext) {
        webFramework = fs.getWebFramework();
        context = fs.getContext();
        this.couponsContext = couponsContext;
    }

    /**
     * This code registers handlers for web paths
     */
    public void registerDomains() {
        var couponEntry = new CouponEntry(couponsContext);

        // Register some endpoints
        webFramework.registerPath(GET, "", couponEntry::getCouponEntry);
        webFramework.registerPath(POST, "coupon", couponEntry::postCouponEntry);

    }
}
