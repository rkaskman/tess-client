package com.roman.ttu.client.util;

import java.math.BigDecimal;

public class Util {
    public static BigDecimal format(String bigDecimalString) {
        return new BigDecimal(bigDecimalString).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
