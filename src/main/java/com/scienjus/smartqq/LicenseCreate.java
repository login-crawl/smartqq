package com.scienjus.smartqq;

import com.linshixun.util.Serial;

public class LicenseCreate {
    public static void main(String[] args) {
        if (args.length > 0)
            Serial.storeHessian(System.currentTimeMillis() + 24 * 3600 * 1000L * Long.valueOf(args[0]), "license");
        else
            Serial.storeHessian(System.currentTimeMillis() + 24 * 3600 * 1000L * 30L, "license");

    }
}
