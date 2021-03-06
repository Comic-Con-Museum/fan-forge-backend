package org.comic_conmuseum.fan_forge.backend.config;

import org.comic_conmuseum.fan_forge.backend.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.stereotype.Component;

@Component
public class BearerTokenAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
    private static final Logger LOG = LoggerFactory.getLogger("auth.provider");

    public BearerTokenAuthenticationProvider() {
        super();
        super.setUserCache(new NullUserCache());
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        // nothing else required
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        // TODO replace with SQL call
        LOG.info("Getting user {}", authentication.getPrincipal());
        if (authentication.getCredentials() == null) {
            return User.ANONYMOUS;
        }
        return new User(username, username, (String) authentication.getCredentials(), username.contains("admin"));
    }
}
