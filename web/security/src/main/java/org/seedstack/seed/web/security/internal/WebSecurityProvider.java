/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.security.internal;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequestBuilder;
import jodd.props.Props;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.SeedRuntime;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.security.internal.SecurityPlugin;
import org.seedstack.seed.security.internal.SecurityProvider;
import org.seedstack.seed.security.internal.SecurityGuiceConfigurer;
import org.seedstack.seed.web.security.SecurityFilter;
import org.seedstack.seed.web.security.internal.shiro.ShiroWebModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This plugins adds web security.
 *
 * @author yves.dautremay@mpsa.com
 */
public class WebSecurityProvider implements SecurityProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSecurityProvider.class);

    private ServletContext servletContext;

    private Collection<Class<? extends Filter>> scannedFilters = new ArrayList<Class<? extends Filter>>();

    private String applicationId;

    private Props props;

    private Class<?> userTokenRepositoryClass;
    private WebSecurityConfig webSecurityConfig;
    private Configuration securityConfiguration;

    @SuppressWarnings("unchecked")
    @Override
    public void init(InitContext initContext) {
        ApplicationPlugin applicationPlugin = initContext.dependency(ApplicationPlugin.class);
        securityConfiguration = applicationPlugin.getConfiguration().subset(SecurityPlugin.SECURITY_PREFIX);
        webSecurityConfig = new WebSecurityConfig();
        webSecurityConfig.init(applicationPlugin.getConfiguration());

        props = applicationPlugin.getProps();
        applicationId = applicationPlugin.getApplication().getId();
        for (Class<?> filterClass : initContext.scannedClassesByAnnotationClass().get(SecurityFilter.class)) {
            if (Filter.class.isAssignableFrom(filterClass)) {
                scannedFilters.add((Class<? extends Filter>) filterClass);
            } else {
                throw new PluginException("Annotated class " + filterClass.getName() + " must implement Filter to be used in a filter chain");
            }
        }

        Collection<Class<?>> userTokenRepositoryImplClasses = initContext.scannedSubTypesByParentClass().get(UserTokenRepository.class);
        for (Class<?> userTokenRepositoryImplClass : userTokenRepositoryImplClasses) {
            if (!webSecurityConfig.getUserTokenRepositoryClass().equals(userTokenRepositoryImplClass)) {
                userTokenRepositoryClass = userTokenRepositoryImplClass;
            }
        }
    }

    @Override
    public void provideContainerContext(Object containerContext) {
        servletContext = ((SeedRuntime)containerContext).contextAs(ServletContext.class);
    }

    @Override
    public void classpathScanRequests(ClasspathScanRequestBuilder classpathScanRequestBuilder) {
        classpathScanRequestBuilder.annotationType(SecurityFilter.class).subtypeOf(UserTokenRepository.class);
    }

    @Override
    public Module provideMainSecurityModule() {
        if (servletContext != null) {
            return Modules.combine(
                    new WebSecurityModule(servletContext, props, scannedFilters, applicationId, webSecurityConfig.getUserTokenRepositoryClass(),
                            new SecurityGuiceConfigurer(securityConfiguration)),
                    ShiroWebModule.guiceFilterModule()
            );
        } else {
            LOGGER.warn("No servlet context available, Web security disabled.");
            return null;
        }
    }

    @Override
    public Module provideAdditionalSecurityModule() {
        return null;
    }
}