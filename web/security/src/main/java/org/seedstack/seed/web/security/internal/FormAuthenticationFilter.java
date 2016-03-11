/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 * <p/>
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.security.internal;

import org.apache.commons.configuration.Configuration;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
import org.seedstack.seed.Application;
import org.seedstack.seed.security.internal.SecurityPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

class FormAuthenticationFilter extends AdviceFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(FormAuthenticationFilter.class);

    private static final String FORM_CONFIG_PREFIX = "form";
    private static final String DEFAULT_USERNAME_PARAM = "username";
    private static final String DEFAULT_PASSWORD_PARAM = "password";
    private static final String DEFAULT_REMEMBER_ME_PARAM = "rememberMe";
    public static final String COOKIE_NAME = "cookie-name";
    public static final String DEFAULT_COOKIE_NAME = "FORM-AUTH-TOKEN";

    private String usernameParam;
    private String passwordParam;
    private String rememberMeParam;
    private String cookieName;
    private final CookieAuthentication cookieAuthentication;
    private final UserTokenRepository userTokenRepository;

    @Inject
    public FormAuthenticationFilter(Application application, UserTokenRepository userTokenRepository) {
        Configuration configuration = application.getConfiguration().subset(SecurityPlugin.SECURITY_PREFIX + "." + FORM_CONFIG_PREFIX);
        this.usernameParam = configuration.getString(DEFAULT_USERNAME_PARAM, DEFAULT_USERNAME_PARAM);
        this.passwordParam = configuration.getString(DEFAULT_PASSWORD_PARAM, DEFAULT_PASSWORD_PARAM);
        this.rememberMeParam = configuration.getString(DEFAULT_REMEMBER_ME_PARAM, DEFAULT_REMEMBER_ME_PARAM);
        this.cookieName = configuration.getString(COOKIE_NAME, DEFAULT_COOKIE_NAME);
        this.userTokenRepository = userTokenRepository;
        /*
         TODO: add the user to the user to the cookie and validate the token for the given user
         check the cookie deletion on logout
         */
        this.cookieAuthentication = new CookieAuthentication(cookieName);
    }

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if (cookieAuthentication.hasValidCookie(httpServletRequest, "supersecuretoken")) {
            return true;
        }
        try {
            SecurityUtils.getSubject().login(createUsernamePasswordToken(request));
            httpServletResponse.addCookie(cookieAuthentication.createCookie(new Date().getTime(), "supersecuretoken"));
        } catch (AuthenticationException e) {
            return denyRequest(request, httpServletResponse);
        }
        return true;
    }

    private boolean denyRequest(ServletRequest request, HttpServletResponse httpServletResponse) {
        LOGGER.trace("Access denied on " + ((HttpServletRequest) request).getRequestURL() + " for " + request.getRemoteAddr());
        httpServletResponse.setStatus(401);
        return false;
    }

    public AuthenticationToken createUsernamePasswordToken(ServletRequest request) {
        String username = getUsername(request);
        String password = getPassword(request);
        boolean rememberMe = isRememberMe(request);
        return new UsernamePasswordToken(username, password, rememberMe);
    }

    protected void postHandle(ServletRequest request, ServletResponse response) throws Exception {
        // nothing to do
    }

    protected String getUsername(ServletRequest request) {
        return WebUtils.getCleanParam(request, usernameParam);
    }

    protected String getPassword(ServletRequest request) {
        return WebUtils.getCleanParam(request, passwordParam);
    }

    protected boolean isRememberMe(ServletRequest request) {
        return WebUtils.isTrue(request, rememberMeParam);
    }
}
