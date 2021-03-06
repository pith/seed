/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.application;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.grapher.NameFactory;
import com.google.inject.grapher.ShortNameFactory;
import com.google.inject.grapher.graphviz.PortIdFactory;
import com.google.inject.grapher.graphviz.PortIdFactoryImpl;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.seedstack.seed.Application;
import org.seedstack.seed.SeedException;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Implementation of the {@link Application} interface.
 *
 * @author adrien.lauer@mpsa.com
 */
class ApplicationImpl implements Application {
    private static final String REGEX_FOR_SUBPACKAGE = "(.*)\\.([^.]*)$";

    private final ApplicationInfo applicationInfo;
    private final File storageRoot;
    private final MapConfiguration configuration;

    @Inject
    private Injector injector;

    public ApplicationImpl(ApplicationInfo applicationInfo, MapConfiguration configuration, File seedStorage) {
        this.applicationInfo = applicationInfo;
        this.configuration = configuration;
        this.storageRoot = seedStorage;
    }

    @Override
    public String getName() {
        return applicationInfo.getName();
    }

    @Override
    public String getId() {
        return applicationInfo.getId();
    }

    @Override
    public String getVersion() {
        return applicationInfo.getVersion();
    }

    @Override
    public File getStorageLocation(String context) {
        if (storageRoot == null) {
            throw SeedException.createNew(ApplicationErrorCode.NO_LOCAL_STORAGE_CONFIGURED).put("context", context);
        }

        File location = new File(storageRoot, context);

        if (!location.exists() && !location.mkdirs()) {
            throw SeedException.createNew(ApplicationErrorCode.UNABLE_TO_CREATE_STORAGE_DIRECTORY).put("path", location.getAbsolutePath());
        }

        if (!location.isDirectory()) {
            throw SeedException.createNew(ApplicationErrorCode.STORAGE_PATH_IS_NOT_A_DIRECTORY).put("path", location.getAbsolutePath());
        }

        if (!location.canWrite()) {
            throw SeedException.createNew(ApplicationErrorCode.STORAGE_DIRECTORY_IS_NOT_WRITABLE).put("path", location.getAbsolutePath());
        }

        return location;
    }

    @Override
    public String getInjectionGraph(String filter) {
        Injector graphvizInjector = injector.createChildInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(NameFactory.class).to(ShortNameFactory.class);
                bind(PortIdFactory.class).to(PortIdFactoryImpl.class);
                bind(SeedGraphvizGrapher.class);
            }
        });

        SeedGraphvizGrapher grapher = graphvizInjector.getInstance(SeedGraphvizGrapher.class);
        StringWriter result = new StringWriter();

        grapher.setOut(new PrintWriter(result));
        if (!Strings.isNullOrEmpty(filter)) {
            grapher.setFilter(filter);
        }

        try {
            grapher.graph(this.injector);
        } catch (IOException e) {
            throw SeedException.wrap(e, ApplicationErrorCode.UNABLE_TO_GENERATE_INJECTION_GRAPH);
        }

        return result.getBuffer().toString();
    }

    @Override
    public String getInjectionGraph() {
        return getInjectionGraph(null);
    }

    @Override
    public Configuration getConfiguration() {
        return this.configuration;
    }

    @Override
    public Configuration getConfiguration(Class<?> clazz) {
        return new MapConfiguration(ImmutableMap.<String, Object>copyOf(getEntityConfiguration(clazz.getName())));
    }

    @Override
    public String substituteWithConfiguration(String value) {
        return configuration.getSubstitutor().replace(value);
    }

    /**
     * Merge property from props section recursively starting by the atomic
     * parent package section coming from entity class name. Properties can be
     * overwritten by using the same key on the subpackage(s) section.
     *
     * @param key props section name
     */
    private void mergeEntityPackageConfiguration(String key, Map<String, String> entityConfiguration) {
        if (key.matches(REGEX_FOR_SUBPACKAGE)) {
            mergeEntityPackageConfiguration(
                    key.replaceFirst(REGEX_FOR_SUBPACKAGE, "$1*"),
                    entityConfiguration);
        }
        Configuration configuration = this.configuration.subset(
                key.replace("*", ".*"));
        if (!configuration.isEmpty()) {
            Iterator<String> keys = configuration.getKeys();
            while (keys.hasNext()) {
                String propertyKey = keys.next();
                entityConfiguration.put(propertyKey,
                        configuration.getString(propertyKey));
            }
        }
    }

    /**
     * Merge property from props section recursively starting by "*" section.
     *
     * @param key props section name
     * @return configuration map
     */
    private Map<String, String> getEntityConfiguration(String key) {
        Configuration configuration = this.configuration.subset("*");
        Map<String, String> entityConfig = new HashMap<String, String>();
        if (!configuration.isEmpty()) {
            Iterator<String> keys = configuration.getKeys();
            while (keys.hasNext()) {
                String propertyKey = keys.next();
                entityConfig.put(propertyKey,
                        configuration.getString(propertyKey));
            }
        }
        mergeEntityPackageConfiguration(key, entityConfig);
        return entityConfig;
    }
}
