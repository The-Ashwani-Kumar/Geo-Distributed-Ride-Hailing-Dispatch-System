package com.ashwani.sharding;

import com.ashwani.enums.Region;

public class RegionContext {
    private static final ThreadLocal<Region> currentRegion = new ThreadLocal<>();

    public static void setRegion(Region region) {
        currentRegion.set(region);
    }

    public static Region getRegion() {
        return currentRegion.get();
    }

    public static void clear() {
        currentRegion.remove();
    }
}
