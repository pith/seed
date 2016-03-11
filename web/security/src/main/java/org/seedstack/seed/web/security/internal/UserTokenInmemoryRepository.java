package org.seedstack.seed.web.security.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserTokenInmemoryRepository implements UserTokenRepository {

    private ConcurrentMap<String, String> userTokens = new ConcurrentHashMap<String, String>();

    @Override
    public void registerToken(String user, String token) {
        userTokens.put(user, token);
    }

    @Override
    public String findToken(String user) {
        return userTokens.get(user);
    }

    @Override
    public void replaceToken(String user, String token) {
        userTokens.replace(user, token);
    }

    @Override
    public void removeToken(String user) {
        userTokens.remove(user);
    }
}
