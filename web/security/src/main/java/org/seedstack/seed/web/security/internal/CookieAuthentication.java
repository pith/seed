package org.seedstack.seed.web.security.internal;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.security.SignatureException;
import java.util.Date;

public class CookieAuthentication {
    public static final String SEPARATOR = ":";

    private final String cookieName;

    public CookieAuthentication(String cookieName) {
        this.cookieName = cookieName;
    }

    public Cookie createCookie(Object principal) {
        long timestamp = new Date().getTime();
        Cookie cookie = new Cookie(cookieName, new CookieValue(principal.toString(), timestamp).toString());
        // TODO if https is enabled (with undertow for instance), enable secure cookie
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        return cookie;
    }

    public boolean hasValidCookie(HttpServletRequest httpServletRequest) {
        Cookie cookie = getAuthCookie(httpServletRequest);
        if (cookie != null) {
            try {
                new CookieValue(cookie.getValue());
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return false;
    }

    public String getPrincipal(HttpServletRequest httpServletRequest) {
        Cookie cookie = getAuthCookie(httpServletRequest);
        if (cookie != null) {
            try {
                return new CookieValue(cookie.getValue()).principal;
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    private Cookie getAuthCookie(HttpServletRequest httpServletRequest) {
        if (httpServletRequest.getCookies() != null) {
            for (Cookie cookie : httpServletRequest.getCookies()) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    class CookieValue {
        private static final String SECURE_TOKEN = "secureToken";

        private String principal;
        private long timestamp;

        public CookieValue(String principal, long timestamp) {
            this.principal = principal;
            this.timestamp = timestamp;
        }

        public CookieValue(String value) {
            String[] splitValue = value.split(SEPARATOR, 3);
            if (splitValue.length == 3) {
                principal = splitValue[0];
                timestamp = new Date(Long.valueOf(splitValue[1])).getTime();
                validateSignature(splitValue[2]);
            }
        }

        public void test(){
            Key key = MacProvider.generateKey();

            String s = Jwts.builder().setSubject("Joe").signWith(SignatureAlgorithm.HS512, key).compact();
        }

        private void validateSignature(String signature) {
            if (!signature.equals(signature())) {
                throw new IllegalArgumentException();
            }
        }

        private String signature() {
            String dataToSign = principal + String.valueOf(timestamp);
            try {
                return Signature.hmac(SECURE_TOKEN, dataToSign);
            } catch (SignatureException e) {
                throw new org.seedstack.seed.security.AuthenticationException(e.getMessage(), e);
            }
        }

        @Override
        public String toString() {
            return principal + SEPARATOR + timestamp + SEPARATOR + signature();
        }
    }
}
