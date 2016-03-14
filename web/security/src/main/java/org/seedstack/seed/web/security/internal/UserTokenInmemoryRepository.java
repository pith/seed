package org.seedstack.seed.web.security.internal;

import jodd.util.Base64;
import org.apache.shiro.subject.PrincipalCollection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserTokenInMemoryRepository implements UserTokenRepository {

    private static ConcurrentMap<String, PrincipalCollection> userTokens = new ConcurrentHashMap<String, PrincipalCollection>();

    @Override
    public String registerToken(Object principal, PrincipalCollection principalCollection) {
        String serializePrincipal = serializePrincipal(principal);
        userTokens.put(serializePrincipal, principalCollection);
        return serializePrincipal;
    }

    private String serializePrincipal(Object principal) {
        try {
            ByteArrayOutputStream serializedPrincipal = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(serializedPrincipal);
            objectOutputStream.writeObject(principal);
            return Base64.encodeToString(serializedPrincipal.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public PrincipalCollection findPrincipal(String principal) {
        return userTokens.get(principal);
    }

    @Override
    public void replaceToken(String principal, PrincipalCollection principalCollection) {
        userTokens.replace(principal, principalCollection);
    }

    @Override
    public void removeToken(String principal) {
        userTokens.remove(principal);
    }
}
