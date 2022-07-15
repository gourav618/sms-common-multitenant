package org.sms.io.common.utils;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

public class UserToken implements AuthenticatedPrincipal, Serializable {

    protected static final String REALM_ACCESS = "realm_access";
    protected static final String SMS_ROLES = "roles";
    protected static final String USERNAME = "preferred_username";
    protected static final String SCOPE = "scope";
    protected static final String TENANTS = "tenants";
    private String currentTenant;

    public UserToken() {
    }

    public UserToken(final Claims claims) {
        setId(claims.getId());
        setUsername(claims.get(USERNAME, String.class));
        if (claims.containsKey(REALM_ACCESS)) {
            Map<String, List<String>> realmAccessClaim = claims.get(REALM_ACCESS, Map.class);
            if (realmAccessClaim.containsKey(SMS_ROLES))
                setRoles(realmAccessClaim.get(SMS_ROLES));
        }
        if (claims.containsKey(TENANTS) )
            setTenants(claims.get(TENANTS, ArrayList.class));
        if (claims.containsKey(SCOPE)) {
            String schema = getTenantFromScope(claims);
            setSchema(schema);
            setCurrentTenant(schema);
        }
    }

    private String getTenantFromScope(Claims claims) {
        String scope = claims.get(SCOPE, String.class);
        String[] scopes = scope.split(" ");
        for (String sc : scopes) {
            if (sc.startsWith("tenant:")) {
                return sc.replaceFirst("tenant:", "");
            }
        }
        return null;
    }

    private String id;

    private List<String> roles;

    private List<String> tenants;

    private String username;

    private String schema;

    public void setCurrentTenant(String currentTenant) {
        this.currentTenant = currentTenant;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getTenants() {
        if (tenants == null) {
            return new ArrayList<>();
        }
        return tenants;
    }

    public void setTenants(List<String> tenants) {
        this.tenants = tenants;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public String getName() {
        return getUsername();
    }

    public String getCurrentTenant() {
        return currentTenant;
    }
}
