package org.comic_con.museum.fcb.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * The controllers all handle normal errors -- no such exhibit, auth
 * required, etc. -- but some errors may still occur, and we want to
 * make sure the caller still gets a (sanitized) response, not a
 * stacktrace or no response at all.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    Logger LOG = LoggerFactory.getLogger("endpoints.exception");
    
    // TODO: Custom error handler for 404 and 401
    // TODO: Explicit mapping for /error
    
    private static class ErrorResponse {
        private String error;
        private String fix;
        
        public ErrorResponse(String error, String fix) {
            this.error = error;
            this.fix = fix;
        }

        public String getError() { return error; }
        public String getFix() { return fix; }

        public void setError(String error) { this.error = error; }
        public void setFix(String fix) { this.fix = fix; }
    }
    
    private static class MissingParamErrorResponse extends ErrorResponse {
        public MissingParamErrorResponse(String paramName, String paramType) {
            super(
                    String.format("Expected %s parameter %s", paramName, paramType),
                    String.format("Pass request parameter %s", paramName)
            );
        }
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> missingParameter(HttpServletRequest req, MissingServletRequestParameterException e) {
        LOG.info("Required parameter %s not given", e.getParameterName());
        
        return ResponseEntity
                .badRequest()
                .body(new MissingParamErrorResponse(e.getParameterName(), e.getParameterType()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        LOG.error("Unmapped error occurred!", e);
        
        throw e;
    }
}
