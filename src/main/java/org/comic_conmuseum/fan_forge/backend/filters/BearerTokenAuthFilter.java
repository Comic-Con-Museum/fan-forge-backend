package org.comic_conmuseum.fan_forge.backend.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BearerTokenAuthFilter extends AbstractAuthenticationProcessingFilter {
    private static final Logger LOG = LoggerFactory.getLogger("filter.auth");
    
    @Value("${ff.require-https}")
    boolean requireHttps;
    
    public BearerTokenAuthFilter(RequestMatcher requestMatcher) {
        super(requestMatcher);
    }
    
    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        // every request requires an auth attempt; even public URLs should have a user attached to them
        return true;
    }
    
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException {
        // NB: We don't use AnonymousAuthenticationToken because that doesn't
        // allow null principals/credentials (for some reason)
        LOG.info("Checking auth on {} {}", req.getMethod(), req.getRequestURI());
        if (requireHttps) {
            if (!req.getProtocol().equalsIgnoreCase("https")) {
                LOG.info("HTTPS required but protocol is {}", req.getProtocol());
                return new UsernamePasswordAuthenticationToken(null, null);
            }
        }
        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            LOG.info("No Authorization header or not Bearer authentication; can't log in");
            return new UsernamePasswordAuthenticationToken(null, null);
        }
        String token = header.substring("Bearer ".length()).trim();
        LOG.info("Request token: '{}'", token);

        return new UsernamePasswordAuthenticationToken(token, token);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        chain.doFilter(request, response);
    }
}
