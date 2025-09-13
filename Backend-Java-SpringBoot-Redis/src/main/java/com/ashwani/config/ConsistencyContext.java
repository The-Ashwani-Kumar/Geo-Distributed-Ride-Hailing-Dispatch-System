package com.ashwani.config;

public class ConsistencyContext {

    private static final ThreadLocal<String> consistencyLevel = new ThreadLocal<>();

    public static void setConsistencyLevel(String level) {
        consistencyLevel.set(level);
    }

    public static String getConsistencyLevel() {
        return consistencyLevel.get();
    }

    public static void clearConsistencyLevel() {
        consistencyLevel.remove();
    }
}
