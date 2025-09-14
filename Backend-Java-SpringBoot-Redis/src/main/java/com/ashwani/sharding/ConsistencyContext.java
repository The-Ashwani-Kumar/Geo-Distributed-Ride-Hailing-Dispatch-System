package com.ashwani.sharding;

import com.ashwani.enums.ConsistencyLevel;

public class ConsistencyContext {
    private static final ThreadLocal<ConsistencyLevel> currentConsistencyLevel = new ThreadLocal<>();

    public static void setConsistencyLevel(ConsistencyLevel level) {
        currentConsistencyLevel.set(level);
    }

    public static ConsistencyLevel getConsistencyLevel() {
        return currentConsistencyLevel.get();
    }

    public static void clear() {
        currentConsistencyLevel.remove();
    }
}
