package org.comic_con.museum.fcb.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthcheckEndpoints {
    // TODO: Actually hit DB and S3 to check health

    @GetMapping("/healthcheck/db")
    public ResponseEntity<String> dbHealthcheck() {
        return ResponseEntity.ok("DB OK");
    }

    @GetMapping("/healthcheck/s3")
    public ResponseEntity<String> s3Healthcheck() {
        return ResponseEntity.ok("S3 OK");
    }

    @GetMapping("/healthcheck")
    public ResponseEntity<Map<String, Boolean>> allHealthcheck() {
        Map<String, Boolean> successes = new HashMap<>();
        successes.put("db", dbHealthcheck().getStatusCode().is2xxSuccessful());
        successes.put("s3", dbHealthcheck().getStatusCode().is2xxSuccessful());
        return new ResponseEntity<>(successes, successes.containsValue(false) ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK);
    }
}
