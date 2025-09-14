package com.ashwani.constant;

public class ApplicationConstant {
    public static final String DRIVER_GEO_KEY_PREFIX = "drivers:geo:"; // Redis GEO key prefix
    public static final String DRIVER_KEY_PREFIX = "drivers:"; // Redis hash key prefix
    public static final String PASSENGER_KEY_PREFIX = "passengers:"; // Redis hash key prefix
    public static final String RIDE_KEY_PREFIX = "rides:"; // Redis hash key prefix
    public static final String CONSISTENCY_LEVEL_HEADER = "X-Consistency-Level";
    public static final String REGION_HEADER = "X-Region";
    public static final String DEFAULT_CONSISTENCY = "strong";
}
