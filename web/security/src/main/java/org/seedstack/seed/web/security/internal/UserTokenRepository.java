package org.seedstack.seed.web.security.internal;

import org.apache.shiro.subject.PrincipalCollection;

public interface UserTokenRepository {

    String registerToken(Object principal, PrincipalCollection principalCollection);

    PrincipalCollection findPrincipal(String user);

    void replaceToken(String principal, PrincipalCollection principalCollection);

    void removeToken(String user);
}
