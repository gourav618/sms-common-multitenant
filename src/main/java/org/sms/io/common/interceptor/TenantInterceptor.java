package org.sms.io.common.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.sms.io.common.utils.TenantContext;
import org.sms.io.common.utils.UserContext;
import org.sms.io.common.utils.UserToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.ValidationException;
import java.util.Optional;

@Component
@Slf4j
public class TenantInterceptor implements HandlerInterceptor {

    private final static String TENANT_HEADER = "SCHEMA";

    @Autowired
    private UserContext userContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        final Authentication authentication = getSecurityContext().getAuthentication();
        if (!(authentication instanceof KeycloakAuthenticationToken)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("Keycloak Token is required or no Token found");
            response.getWriter().flush();
            log.info("Request failed: Keycloak Token is required or no Token found");
            return false;
        }

        final Optional<UserToken> optionalUserToken = userContext.getToken();

        if (optionalUserToken.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("Invalid Token");
            response.getWriter().flush();
            log.info("Request failed: Invalid Token");
            return false;
        }

        UserToken token = optionalUserToken.get();

        userContext.setBasicUser(token.getUsername());
        userContext.setBasicTenant(token.getCurrentTenant());

        String tenant = userContext.getTenant();
        String user = userContext.getUser();

        if (tenant == null || user == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("No tenant or user supplied");
            response.getWriter().flush();
            log.info("Request failed: No tenant or user supplied");
            return false;
        }

        if (token.getTenants() == null || token.getTenants().isEmpty() || !token.getTenants().contains("tenant:"+tenant)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("User does not have access to the tenant");
            response.getWriter().flush();
            log.info("Request failed: User does not have access to the tenant");
            return false;
        } else {
            log.info("User has been granted access to the "+ token.getCurrentTenant()+" tenant");
            TenantContext.setCurrentTenant(tenant);
            return true;
        }
    }

    private SecurityContext getSecurityContext() {
        return SecurityContextHolder.getContext();
    }
}