package com.coupons;

import com.coupons.database.DatabaseMigration;
import com.coupons.utils.CouponsContext;
import com.renomad.minum.state.Constants;
import com.renomad.minum.testing.StopwatchUtils;
import com.renomad.minum.web.FullSystem;

public class Main {

    public static void main(String[] args) {
        StopwatchUtils stopWatch = new StopwatchUtils();
        stopWatch.startTimer();

        FullSystem fs = FullSystem.initialize();
        var couponsContext = CouponsContext.build(fs);
        new DatabaseMigration(couponsContext).run();
        new Registry(fs, couponsContext).registerDomains();

        // show an indicator that the system is ready to use
        Constants constants = fs.getContext().getConstants();
        System.out.printf(
                "\n\nSystem is ready after %d milliseconds.  " +
                        "Access at http://%s:%d or https://%s:%d\n\n",
                stopWatch.stopTimer(),
                constants.hostName,
                constants.serverPort,
                constants.hostName,
                constants.secureServerPort);

        fs.block();
    }
}
