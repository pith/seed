/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.security;

import com.jayway.restassured.response.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.seedstack.seed.it.AbstractSeedWebIT;

import java.net.URL;

import static com.jayway.restassured.RestAssured.expect;

public class FormAuthenticationIT extends AbstractSeedWebIT {

    public static final String DEFAULT_COOKIE_NAME = "FORM-AUTH-TOKEN";
    public static final String PATH = "api/my-resource";
    @ArquillianResource
    private URL baseURL;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap
                .create(WebArchive.class)
                .addAsResource("form-auth.props", "META-INF/configuration/form-auth.props");
    }

    @Test
    @RunAsClient
    public void request_without_auth_get_401() throws Exception {
        expect()
                .statusCode(401)
                .when()
                    .get(baseURL.toString() + PATH);
    }

    @Test
    @RunAsClient
    public void auth_with_form_succeed() throws Exception {
        expect()
                .statusCode(200)
                .given()
                    .param("username", "john")
                    .param("password", "passw0rd")
                .when()
                    .get(baseURL.toString() + PATH);
    }

    @Test
    @RunAsClient
    public void auth_with_with_cookie_works() throws Exception {
        Response response = expect()
                .statusCode(200)
                .given()
                .param("username", "john")
                .param("password", "passw0rd")
                .when()
                .get(baseURL.toString() + PATH);

        String token = response.getCookies().get(DEFAULT_COOKIE_NAME);

        expect()
                .statusCode(200)
                .given()
                    .cookie(DEFAULT_COOKIE_NAME, token)
                .when()
                    .get(baseURL.toString() + PATH);
    }

    @Test
    @RunAsClient
    public void jwt() throws Exception {
        Response response = expect()
                .statusCode(200)
                .given()
                .param("username", "john")
                .param("password", "passw0rd")
                .when()
                .get(baseURL.toString() + "login");

        String jwt = response.asString();

        expect()
                .statusCode(200)
                .given()
                    .header("Authentication", "Bearer " + jwt)
                .when()
                    .get(baseURL.toString() + PATH);
    }
}
