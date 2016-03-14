package org.seedstack.seed.web.security.internal;

import org.seedstack.seed.security.*;
import org.seedstack.seed.security.principals.PrincipalProvider;

import java.util.Collection;
import java.util.Set;

public class JwtRealm implements Realm {
    @Override
    public Set<String> getRealmRoles(PrincipalProvider<?> identityPrincipal, Collection<PrincipalProvider<?>> otherPrincipals) {
        return null;
    }

    @Override
    public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        return null;
    }

    @Override
    public RoleMapping getRoleMapping() {
        return null;
    }

    @Override
    public RolePermissionResolver getRolePermissionResolver() {
        return null;
    }

    @Override
    public Class<? extends AuthenticationToken> supportedToken() {
        return null;
    }
}
