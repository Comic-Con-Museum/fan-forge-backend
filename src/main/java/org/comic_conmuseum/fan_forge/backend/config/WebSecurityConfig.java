package org.comic_conmuseum.fan_forge.backend.config;

import org.comic_conmuseum.fan_forge.backend.filters.BearerTokenAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

// https://medium.com/@nydiarra/secure-a-spring-boot-rest-api-with-json-web-token-reference-to-angular-integration-e57a25806c50

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final RequestMatcher PUBLIC_URLS = new OrRequestMatcher(
            // CORS uses OPTIONS to check what's allowed, so we always allow
            // those with no auth
            new AntPathRequestMatcher("/**", "OPTIONS"),
            // Everyone needs access to POST /login to get bearer tokens
            new AntPathRequestMatcher("/login", "POST"),
            // Getting model details and the feeds is available to everyone
            new AntPathRequestMatcher("/exhibit/*", "GET"),
            new AntPathRequestMatcher("/artifact/*", "GET"),
            new AntPathRequestMatcher("/comment/*", "GET"),
            new AntPathRequestMatcher("/feed/*", "GET"),
            // Healthcheck is used for automatic deployment and monitoring, and
            //  shouldn't require auth
            new AntPathRequestMatcher("/healthcheck/**", "GET"),
            // This endpoint is how images are loaded by the frontend
            new AntPathRequestMatcher("/image/*", "GET"),
            // Can get the list of all tags without being signed in, since it's on the feed
            new AntPathRequestMatcher("/tags", "GET"),
            // And /error is the default error page; it should never be shown,
            //  but in case it is, we don't want to give a 404.
            new AntPathRequestMatcher("/error", "GET")
    );

    private final RequestMatcher ADMIN_URLS = new AntPathRequestMatcher("/admin/**");

    private final RequestMatcher AUTH_REQ_URLS = new NegatedRequestMatcher(PUBLIC_URLS);

    private final BearerTokenAuthenticationProvider authProvider;

    @Autowired
    public WebSecurityConfig(BearerTokenAuthenticationProvider authProvider) {
        this.authProvider = authProvider;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .sessionManagement()
                    // No sessions, because we want them to provide the auth token
                    // every time.
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                    .defaultAuthenticationEntryPointFor(new Http403ForbiddenEntryPoint(), AUTH_REQ_URLS)
                .and()
                .authenticationProvider(authProvider)
                .addFilterBefore(bearerTokenAuthFilter(), AnonymousAuthenticationFilter.class)
                .authorizeRequests()
                    // Require admin privileges to hit these endpoints
                    .requestMatchers(ADMIN_URLS).hasRole("ADMIN")
                    // Require auth (but not admin) for the rest
                    .requestMatchers(AUTH_REQ_URLS).hasRole("USER")
                    // No auth needed on the no-login-required endpoints
                    .requestMatchers(PUBLIC_URLS).permitAll()
                .and()
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .logout().disable();
    }

    BearerTokenAuthFilter bearerTokenAuthFilter() throws Exception {
        // create the token auth filter for attachment
        BearerTokenAuthFilter filter = new BearerTokenAuthFilter(AUTH_REQ_URLS);
        filter.setAuthenticationManager(authenticationManager());
        SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler();
        successHandler.setRedirectStrategy((req, res, url) -> {});
        filter.setAuthenticationSuccessHandler(successHandler);
        return filter;
    }
}
