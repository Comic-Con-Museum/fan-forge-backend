package org.comic_con.museum.fcb.controllers;

import org.comic_con.museum.fcb.controllers.inputs.LoginParams;
import org.comic_con.museum.fcb.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserAuthEndpoints {
    private Logger LOG = LoggerFactory.getLogger(UserAuthEndpoints.class);

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<String> login(@RequestBody LoginParams info) {
        LOG.info("Logging in as user {}", info.username);
        return ResponseEntity.ok(info.username);
    }

    @RequestMapping(value = "/login", method = RequestMethod.DELETE)
    public ResponseEntity<Boolean> logout(@AuthenticationPrincipal User user) {
        LOG.info("Logging out user {}", user.getUsername());
        return ResponseEntity.ok().build();
    }
}
