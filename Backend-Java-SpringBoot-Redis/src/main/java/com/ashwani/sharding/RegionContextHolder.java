package com.ashwani.sharding;

import com.ashwani.enums.Region;

public class RegionContextHolder {

    private static final ThreadLocal<Region> regionThreadLocal = new ThreadLocal<>();

    public static void setRegion(Region region) {
        regionThreadLocal.set(region);
    }

    public static Region getRegion() {
        return regionThreadLocal.get();
    }

    public static void clear() {
        regionThreadLocal.remove();
    }
}
