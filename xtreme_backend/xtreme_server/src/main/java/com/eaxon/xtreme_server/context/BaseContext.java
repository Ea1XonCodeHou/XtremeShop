package com.eaxon.xtreme_server.context;

public class BaseContext {

    private static final ThreadLocal<Long> currentId = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        currentId.set(id);
    }

    public static Long getCurrentId() {
        return currentId.get();
    }

    public static void removeCurrentId() {
        currentId.remove();
    }
}
