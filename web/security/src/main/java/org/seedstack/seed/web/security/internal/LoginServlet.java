package org.seedstack.seed.web.security.internal;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import jodd.util.Base64;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.seedstack.seed.security.SecuritySupport;
import org.seedstack.seed.web.WebServlet;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.Key;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    public static final Key SECRET_KEY = MacProvider.generateKey();

    @Inject
    private SecuritySupport securitySupport;

    private UserTokenRepository userTokenRepository = new UserTokenInMemoryRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (securitySupport.isAuthenticated()) {
            Subject subject = SecurityUtils.getSubject();
            userTokenRepository.registerToken(subject.getPrincipal(), subject.getPrincipals());
            String jwt = getJwt(subject);
            resp.setStatus(200);
            resp.setContentLength(jwt.length());
            resp.setContentType("text/plain");
            resp.getWriter().write(jwt);
        } else {
            resp.setStatus(401);
        }
    }

    private String getJwt(Subject subject) {
        return Jwts.builder().setSubject(serializePrincipal(subject)).signWith(SignatureAlgorithm.HS512, SECRET_KEY).compact();
    }

    private String serializePrincipal(Subject subject) {
        try {
            ByteArrayOutputStream serializedPrincipal = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(serializedPrincipal);
            objectOutputStream.writeObject(subject.getPrincipal());
            return Base64.encodeToString(serializedPrincipal.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
