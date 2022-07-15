package org.sms.io.common.utils;

import java.util.Optional;

public class TenantContext {

    public static final String DEFAULT_TENANT_ID = "smsdev";

    private static ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setCurrentTenant(String tenant) {
        currentTenant.set(tenant);
    }

    public static String getCurrentTenant() {
        return Optional.ofNullable(currentTenant.get())
                .orElse(DEFAULT_TENANT_ID);
    }

    public static void clearTenant() {
        currentTenant.remove();
    }
}
