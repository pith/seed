/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.undertow.internal;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.spi.configuration.ConfigurationProvider;
import org.seedstack.seed.crypto.spi.SSLProvider;

import java.util.Collection;

/**
 * The Undertow plugin is responsible to retrieve the undertow configuration.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class UndertowPlugin extends AbstractPlugin {

    private static final String CONFIGURATION_PREFIX = "org.seedstack.seed.server";
    public static final String NAME = "undertow";
    private ServerConfig serverConfig;

    @Override
    public String name() {
        return "undertow";
    }

    @Override
    public InitState init(InitContext initContext) {
        Configuration serverConfig = initContext.dependency(ConfigurationProvider.class)
                .getConfiguration().subset(CONFIGURATION_PREFIX);
        this.serverConfig = new ServerConfigFactory().create(serverConfig, initContext.dependency(SSLProvider.class));
        return InitState.INITIALIZED;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    @Override
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(ConfigurationProvider.class, SSLProvider.class);
    }

}
