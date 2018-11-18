package org.comic_conmuseum.fan_forge.backend.endpoints;

import org.comic_conmuseum.fan_forge.backend.endpoints.inputs.LoginParams;
import org.comic_conmuseum.fan_forge.backend.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class UserAuthEndpoints {
    private static final Logger LOG = LoggerFactory.getLogger("endpoints.auth");
    @Value("${security.pwd.secret}")
    private String secret;

    private static class TokenData {
        public final String token;
        public final int expires;

        TokenData(String token, int expires) {
            this.token = token;
            this.expires = expires;
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<TokenData> login(@RequestBody LoginParams info, HttpServletRequest req) {
        LOG.info("Logging in as user {} with password length {}", info.getUsername(), info.getPassword().length);
        info.zeroPassword(); // don't let the sensitive data linger in memory

        return ResponseEntity.ok(new TokenData(info.getUsername(), Integer.MAX_VALUE));
    }

    @RequestMapping(value = "/login", method = RequestMethod.DELETE)
    public ResponseEntity<Boolean> logout(@AuthenticationPrincipal User user) {
        LOG.info("Logging out user {}", user.getUsername());
        return ResponseEntity.ok().build();
    }
}
