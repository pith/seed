/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.security.fixtures;

import org.seedstack.seed.security.SecuritySupport;
import org.seedstack.seed.web.WebServlet;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/my-resource")
public class MyResource extends HttpServlet {

    public static final String HELLO_WORLD = "Hello world !";

    @Inject
    private SecuritySupport securitySupport;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (securitySupport.isAuthenticated()) {
            resp.setStatus(200);
            resp.setContentLength(HELLO_WORLD.length());
            resp.setContentType("text/plain");
            resp.getWriter().write(HELLO_WORLD);
        } else {
            resp.setStatus(401);
        }
    }
}
