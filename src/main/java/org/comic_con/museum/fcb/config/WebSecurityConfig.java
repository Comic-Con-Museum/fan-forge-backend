package org.comic_con.museum.fcb.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
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
            new AntPathRequestMatcher("/login", "POST"),
            new AntPathRequestMatcher("/exhibit/*", "GET"),
            new AntPathRequestMatcher("/feed/*", "GET")
    );

    private final RequestMatcher ADMIN_URLS = new AntPathRequestMatcher("/admin/**");

    private final RequestMatcher AUTH_REQ_URLS = new NegatedRequestMatcher(PUBLIC_URLS);

    private final TokenAuthProvider authProvider;

    public WebSecurityConfig(@Autowired TokenAuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authProvider);
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().requestMatchers(PUBLIC_URLS);
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
                    .defaultAuthenticationEntryPointFor(http403ForbiddenEntryPoint(), AUTH_REQ_URLS)
                .and()
                .authenticationProvider(authProvider)
                .addFilterBefore(bearerTokenAuthFilter(), AnonymousAuthenticationFilter.class)
                .authorizeRequests()
                    // Require admin privileges to hit these endpoints
                    .requestMatchers(ADMIN_URLS).hasRole("ADMIN")
                    // No auth needed on the no-login-required endpoints
                    .requestMatchers(PUBLIC_URLS).permitAll()
                    // Require auth (but not admin) for the rest
                    .requestMatchers(AUTH_REQ_URLS).authenticated()
                .and()
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .logout().disable();
    }

    @Bean
    FilterRegistrationBean<BearerTokenAuthFilter> disableAutoRegistration(BearerTokenAuthFilter filter) {
        FilterRegistrationBean<BearerTokenAuthFilter> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }

    @Bean
    AuthenticationSuccessHandler authSuccessHandler() {
        SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler();
        successHandler.setRedirectStrategy((req, res, url) -> {});
        return successHandler;
    }

    @Bean
    BearerTokenAuthFilter bearerTokenAuthFilter() throws Exception {
        BearerTokenAuthFilter filter = new BearerTokenAuthFilter(AUTH_REQ_URLS);
        filter.setAuthenticationManager(authenticationManager());
        filter.setAuthenticationSuccessHandler(authSuccessHandler());
        return filter;
    }

    @Bean
    AuthenticationEntryPoint http403ForbiddenEntryPoint() {
        return new Http403ForbiddenEntryPoint();
    }
}
