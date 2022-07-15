package org.sms.io.common.utils;

import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.OidcKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserContext {

    private String basicTenant;

    private String basicUser;

    private UserToken userToken;

    @Autowired
    private ClientJwtTokenUtility clientJwtTokenUtility;

    public String getTenant() {
        return Stream.of(getToken()
                    .map(UserToken::getCurrentTenant),Optional.ofNullable(basicTenant))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(null);
    }

    public String getUser() {
        return Stream.of(getToken()
                        .map(UserToken::getUsername),Optional.ofNullable(basicUser))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(null);
    }


    public Optional<UserToken> getToken() {
        if (this.userToken != null) {
            return Optional.of(userToken);
        }
        Optional<String> jwtString = getJwtString();
        if (jwtString.isPresent()) {
            UserToken tokenFromString = clientJwtTokenUtility.getTokenFromString(jwtString.get());
            if (tokenFromString != null) {
                userToken = tokenFromString;
                return Optional.of(tokenFromString);
            }
        }
        return Optional.empty();
    }

    private Optional<String> getJwtString() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Optional.of(authentication)
                .filter(KeycloakAuthenticationToken.class::isInstance)
                .map(KeycloakAuthenticationToken.class::cast)
                .map(KeycloakAuthenticationToken::getAccount)
                .map(OidcKeycloakAccount::getKeycloakSecurityContext)
                .map(KeycloakSecurityContext::getTokenString);
    }

    public String getBasicTenant() {
        return basicTenant;
    }

    public void setBasicTenant(String basicTenant) {
        this.basicTenant = basicTenant;
    }

    public String getBasicUser() {
        return basicUser;
    }

    public void setBasicUser(String basicUser) {
        this.basicUser = basicUser;
    }
}
