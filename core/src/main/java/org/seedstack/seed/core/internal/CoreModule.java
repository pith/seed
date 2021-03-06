/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.util.Types;
import org.seedstack.seed.DiagnosticManager;
import org.seedstack.seed.LifecycleListener;
import org.seedstack.seed.core.utils.SeedCheckUtils;
import org.seedstack.seed.spi.dependency.DependencyProvider;
import org.seedstack.seed.spi.dependency.Maybe;
import org.seedstack.seed.spi.diagnostic.DiagnosticInfoCollector;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Guice module for SEED core functionality.
 *
 * @author adrien.lauer@mpsa.com
 */
class CoreModule extends AbstractModule {
    private final Collection<Module> subModules;
    private final DiagnosticManager diagnosticManager;
    private final Map<String, DiagnosticInfoCollector> diagnosticInfoCollectors;
    private final Map<Class<?>, Maybe<? extends DependencyProvider>> optionalDependencies;
    private final Set<Class<? extends LifecycleListener>> lifecycleListenerClasses;

    CoreModule(Collection<Module> subModules, Set<Class<? extends LifecycleListener>> lifecycleListenerClasses, DiagnosticManager diagnosticManager, Map<String, DiagnosticInfoCollector> diagnosticInfoCollectors, Map<Class<?>, Maybe<? extends DependencyProvider>> optionalDependencies) {
        this.subModules = subModules;
        this.lifecycleListenerClasses = lifecycleListenerClasses;
        this.diagnosticManager = diagnosticManager;
        this.diagnosticInfoCollectors = diagnosticInfoCollectors;
        this.optionalDependencies = optionalDependencies;
    }

    @Override
    protected void configure() {
        install(new PrivateModule() {
            @Override
            protected void configure() {
                // Diagnostics
                MapBinder<String, DiagnosticInfoCollector> diagnosticInfoCollectorMapBinder = MapBinder.newMapBinder(binder(), String.class, DiagnosticInfoCollector.class);
                for (Map.Entry<String, DiagnosticInfoCollector> diagnosticInfoCollectorEntry : diagnosticInfoCollectors.entrySet()) {
                    diagnosticInfoCollectorMapBinder.addBinding(diagnosticInfoCollectorEntry.getKey()).toInstance(diagnosticInfoCollectorEntry.getValue());
                }
                bind(DiagnosticManager.class).toInstance(diagnosticManager);
                expose(DiagnosticManager.class);
            }
        });

        // Static utils
        requestStaticInjection(SeedCheckUtils.class);

        // Logging
        bindListener(Matchers.any(), new LoggingTypeListener());

        // Install detected modules
        for (Module subModule : subModules) {
            install(subModule);
        }

        // Lifecycle listeners
        Multibinder<LifecycleListener> lifecycleListenerMultibinder = Multibinder.newSetBinder(binder(), LifecycleListener.class);
        for (Class<? extends LifecycleListener> lifecycleListenerClass : lifecycleListenerClasses) {
            lifecycleListenerMultibinder.addBinding().to(lifecycleListenerClass);
        }

        // Optional dependencies
        for (final Entry<Class<?>, Maybe<? extends DependencyProvider>> dependency : this.optionalDependencies.entrySet()) {
            @SuppressWarnings("unchecked")
            TypeLiteral<Maybe<? extends DependencyProvider>> typeLiteral = (TypeLiteral<Maybe<? extends DependencyProvider>>) TypeLiteral.get(Types.newParameterizedType(Maybe.class, dependency.getKey()));
            bind(typeLiteral).toInstance(dependency.getValue());
        }
    }
}
