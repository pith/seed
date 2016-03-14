package org.seedstack.seed.web.security.internal;

import io.nuun.kernel.api.plugin.PluginException;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.security.internal.SecurityPlugin;
import org.seedstack.seed.spi.dependency.Maybe;

public class WebSecurityConfig {

    public static final long DEFAULT_GLOBAL_SESSION_TIMEOUT = 1000 * 60 * 15;

    private Class<? extends UserTokenRepository> userTokenRepository;
    private long sessionTimeout;

    void init(Configuration configuration) {
        Configuration securityConfiguration = configuration.subset(SecurityPlugin.SECURITY_PREFIX);
        setUserTokenRepository(securityConfiguration);
        securityConfiguration.getLong("sessions.timeout", DEFAULT_GLOBAL_SESSION_TIMEOUT);
    }

    @SuppressWarnings("unchecked")
    private void setUserTokenRepository(Configuration securityConfiguration) {
        String repositoryClassName = securityConfiguration.getString("form.user-token-repository", UserTokenInMemoryRepository.class.getCanonicalName());
        Maybe<Class<?>> repositoryClass = SeedReflectionUtils.forName(repositoryClassName);
        if (repositoryClass.isPresent() && UserTokenRepository.class.isAssignableFrom(repositoryClass.get())) {
            userTokenRepository = (Class<? extends UserTokenRepository>) repositoryClass.get();
        } else {
            // TODO SeedException
            throw new PluginException("No UserTokenRepository implementation available");
        }
    }

    public Class<? extends UserTokenRepository> getUserTokenRepositoryClass() {
        return userTokenRepository;
    }

    public void setUserTokenRepository(Class<? extends UserTokenRepository> userTokenRepository) {
        this.userTokenRepository = userTokenRepository;
    }

    public long getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
}
