package org.comic_con.museum.fcb.beans;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

// https://github.com/nydiarra/springboot-jwt/blob/66f578c1b8b2016c442c43564a03da1b64c7fea4/src/main/java/com/nouhoun/springboot/jwt/integration/service/impl/AppUserDetailsService.java#L20
public class InMemoryUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        switch (s) {
            case "testA"
        }
    }
}
