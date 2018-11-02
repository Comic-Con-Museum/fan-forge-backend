package org.comic_con.museum.fcb.config;

import org.comic_con.museum.fcb.models.User;
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
    private final Logger LOG = LoggerFactory.getLogger("auth.provider");

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
        LOG.info("Getting user {}", username);
        return new User(username.hashCode(), username, authentication.getCredentials().toString(), username.equals("admin"));
    }
}
