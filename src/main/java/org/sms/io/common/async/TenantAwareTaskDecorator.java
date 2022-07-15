package org.sms.io.common.async;

import org.sms.io.common.utils.TenantContext;
import org.springframework.core.task.TaskDecorator;

public class TenantAwareTaskDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        String tenantName = TenantContext.getCurrentTenant();
        return () -> {
            try {
                TenantContext.setCurrentTenant(tenantName);
                runnable.run();
            } finally {
                TenantContext.setCurrentTenant(null);
            }
        };
    }
}
