package org.comic_con.museum.fcb.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
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

    public BearerTokenAuthFilter(RequestMatcher requestMatcher) {
        super(requestMatcher);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException {
        LOG.info("Checking auth on URL {}", req.getRequestURI());
        String header = req.getHeader("Authorization");
        if (header == null) {
            LOG.info("No Authorization header, can't log in");
            throw new BadCredentialsException("No Authorization header");
        }
        if (!header.startsWith("Bearer ")) {
            // ...then it's not bearer token authorization
            LOG.info("Non-Bearer Authorization header: '{}'", header);
            throw new BadCredentialsException("Authorization header is not Bearer type");
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
