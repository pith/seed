/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import com.damnhandy.uri.template.UriTemplate;
import com.damnhandy.uri.template.UriTemplateBuilder;
import org.seedstack.seed.rest.Rel;
import org.seedstack.seed.rest.hal.Link;
import org.seedstack.seed.rest.internal.jsonhome.HintScanner;
import org.seedstack.seed.rest.internal.jsonhome.Hints;
import org.seedstack.seed.rest.internal.jsonhome.Resource;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Scans the JAX-RS resources for building JSON-HOME resources and HAL links.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class ResourceScanner {

    private static final RelSpecification METHOD_OR_CLASS_HAS_REL = new RelSpecification();
    private static final JsonHomeSpecification EXPOSE_AS_ENTRY_POINT = new JsonHomeSpecification();

    private final Map<String, List<Method>> resourceByRel = new HashMap<String, List<Method>>();
    private final Map<String, Resource> jsonHomeResources = new HashMap<String, Resource>();
    private final Map<String, Link> halLinks = new HashMap<String, Link>();

    private final RestConfiguration restConfiguration;

    /**
     * Constructor.
     *
     * @param restConfiguration the REST configuration object.
     */
    public ResourceScanner(RestConfiguration restConfiguration) {
        this.restConfiguration = restConfiguration;
    }

    /**
     * Scans a collection of resources.
     *
     * @param classes the resource to scan
     * @return itself
     */
    public ResourceScanner scan(final Collection<Class<?>> classes) {
        for (Class<?> aClass : classes) {
            collectHttpMethodsWithRel(aClass);
        }
        buildJsonHomeResources();
        buildHalLink();
        return this;
    }

    private void collectHttpMethodsWithRel(Class<?> aClass) {
        for (Method method : aClass.getDeclaredMethods()) {
            if (METHOD_OR_CLASS_HAS_REL.isSatisfiedBy(method)) {
                Rel relAnnotation = RESTReflect.findRel(method);
                if (relAnnotation == null || "".equals(relAnnotation.value())) {
                    throw new IllegalStateException("Missing rel value on " + method.toGenericString());
                }
                registerMethod(relAnnotation.value(), method);
            }
        }
    }

    private void registerMethod(String rel, Method method) {
        List<Method> methods = resourceByRel.get(rel);
        if (methods == null) {
            methods = new ArrayList<Method>();
        }
        methods.add(method);
        resourceByRel.put(rel, methods);
    }

    /**
     * Returns the JSON-HOME resources.
     *
     * @return resource map
     */
    public Map<String, Resource> jsonHomeResources() {
        return jsonHomeResources;
    }

    /**
     * Returns the HAL links.
     *
     * @return the link map
     */
    public Map<String, Link> halLinks() {
        return halLinks;
    }

    private void buildJsonHomeResources() {
        for (Map.Entry<String, List<Method>> entry : resourceByRel.entrySet()) {
            // Extends the rel with baseRel
            String rel = entry.getKey();
            String absoluteRel = UriBuilder.uri(restConfiguration.getBaseRel(), rel);

            Resource resource = null;
            List<Method> methods = entry.getValue();
            for (Method method : methods) {
                Resource currentResource = buildJsonHomeResource(restConfiguration.getBaseParam(), absoluteRel, method);
                if (resource == null) {
                    resource = currentResource;
                } else {
                    resource.merge(currentResource);
                }
            }
            if (resource != null) {
                jsonHomeResources.put(absoluteRel, resource);
            }
        }
    }

    private Resource buildJsonHomeResource(String baseParam, String rel, Method method) {
        Resource currentResource = null;

        if (EXPOSE_AS_ENTRY_POINT.isSatisfiedBy(method)) {

            String path = RESTReflect.findPath(method);
            if (path == null) {
                return null;
            }

            Hints hints = new HintScanner().findHint(method);

            String absolutePath = UriBuilder.uri(restConfiguration.getRestPath(), path);

            if (isTemplated(absolutePath)) {
                Map<String, String> pathParams = RESTReflect.findPathParams(baseParam, method);
                Map<String, String> queryParams = RESTReflect.findQueryParams(baseParam, method);
                currentResource = new Resource(rel, absolutePath, pathParams, queryParams, hints);
            } else {
                currentResource = new Resource(rel, absolutePath, hints);
            }
        }
        return currentResource;
    }

    private boolean isTemplated(String path) {
        return UriTemplate.fromTemplate(path).expressionCount() > 0;
    }

    private void buildHalLink() {
        for (Map.Entry<String, List<Method>> entry : resourceByRel.entrySet()) {
            String rel = entry.getKey();
            List<Method> methodsByRel = entry.getValue();

            String path = RESTReflect.findPath(methodsByRel.get(0));
            if (path == null) {
                throw new IllegalStateException("Path not found for rel: " + rel);
            }
            UriTemplateBuilder uriTemplateBuilder = UriTemplate.buildFromTemplate(path);

            Set<String> queryParams = findAllQueryParamsForRel(methodsByRel);
            if (!queryParams.isEmpty()) {
                uriTemplateBuilder.query(queryParams.toArray(new String[queryParams.size()]));
            }

            String absolutePath = UriBuilder.uri(restConfiguration.getRestPath(), uriTemplateBuilder.build().getTemplate());

            halLinks.put(rel, new Link(absolutePath));
        }
    }

    private Set<String> findAllQueryParamsForRel(List<Method> methodsByRel) {
        // A resource correspond to one URI but one URI can correspond to multiple method
        // so we have to look on all the methods to find the query parameters
        Set<String> queryParams = new HashSet<String>();
        for (Method method : methodsByRel) {
            queryParams.addAll(RESTReflect.findQueryParams("", method).keySet());
        }
        return queryParams;
    }
}
