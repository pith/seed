package org.seedstack.seed.web.security.internal;

public interface UserTokenRepository {

    void registerToken(String user, String token);

    String findToken(String user);

    void replaceToken(String user, String token);

    void removeToken(String user);
}
