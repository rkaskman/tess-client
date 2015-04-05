package com.roman.ttu.client;

public class ApplicationHolder {

    private static Application application;

    static void set(Application application) {
        ApplicationHolder.application = application;
    }

    public static Application get() {
        return application;
    }
}
