package org.seedstack.seed.web.security.internal;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.security.SignatureException;
import java.util.Date;

public class CookieAuthentication {

    public static final String SEPARATOR = ":";

    private final String cookieName;

    public CookieAuthentication(String cookieName) {
        this.cookieName = cookieName;
    }

    public Cookie createCookie(long timestamp, String token) {
        Cookie cookie = new Cookie(cookieName, timestamp + SEPARATOR + signTimestampWithToken(timestamp, token));
        // TODO if https is enabled (with undertow for instance), enable secure cookie
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        return cookie;
    }

    private String signTimestampWithToken(long timestamp, String token) {
        try {
            return Signature.hmac(token, String.valueOf(timestamp));
        } catch (SignatureException e) {
            throw new org.seedstack.seed.security.AuthenticationException(e.getMessage(), e);
        }
    }

    public boolean hasValidCookie(HttpServletRequest httpServletRequest, String token) {
        Cookie cookie = getAuthCookie(httpServletRequest);
        if (cookie != null) {
            String[] splitValue = cookie.getValue().split(SEPARATOR, 2);
            if (splitValue.length == 2) {
                long timestamp = new Date(Long.valueOf(splitValue[0])).getTime();
                String signedToken = splitValue[1];
                return signedToken.equals(signTimestampWithToken(timestamp, token));
            }
        }
        return false;
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
}
