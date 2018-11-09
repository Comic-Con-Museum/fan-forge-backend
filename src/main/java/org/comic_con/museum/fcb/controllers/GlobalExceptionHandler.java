package org.comic_con.museum.fcb.controllers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;

/**
 * The controllers all handle normal errors -- no such exhibit, auth
 * required, etc. -- but some errors may still occur, and we want to
 * make sure the caller still gets a (sanitized) response, not a
 * stacktrace or no response at all.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger("endpoints.exception");
    
    // TODO: Custom error handler for 404 and 401
    // TODO: Explicit mapping for /error
    
    private static class ErrorResponse {
        private String error;
        @JsonInclude(JsonInclude.Include.NON_NULL)
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
    
    private static class InternalServerError extends ErrorResponse {
        public InternalServerError(String briefDesc) {
            super(
                    briefDesc,
                    "Contact the developers immediately -- see README.md"
            );
        }
    }
    
    private static class MissingParamErrorResponse extends ErrorResponse {
        public MissingParamErrorResponse(String paramName, String paramType) {
            super(
                    String.format("Expected %s parameter %s", paramType, paramName),
                    String.format("Pass request parameter %s; see documentation for more information", paramName)
            );
        }
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> missingParameter(HttpServletRequest req, MissingServletRequestParameterException e) {
        LOG.info("Required parameter {} not given", e.getParameterName());
        
        return new ResponseEntity<>(
                new MissingParamErrorResponse(e.getParameterName(), e.getParameterType()),
                HttpStatus.BAD_REQUEST
        );
    }
    
    @ExceptionHandler({EmptyResultDataAccessException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> noResult(HttpServletRequest req, Exception e) {
        return ResponseEntity.notFound().build();
    }
    
    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ErrorResponse> invalidPOJO(HttpServletRequest req, JsonProcessingException e) {
        LOG.error("Conversion of POJO to JSON failed", e);
        return new ResponseEntity<>(new InternalServerError(
                "The server failed to generate the normal JSON response"
        ), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponse> nyi(HttpServletRequest req, UnsupportedOperationException e) {
        LOG.error("Unimplemented endpoint hit: {} {}", req.getMethod(), req.getRequestURI());
        
        return new ResponseEntity<>(new InternalServerError(
                "The endpoint hasn't yet been coded"
        ), HttpStatus.NOT_IMPLEMENTED);
    }
    
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> methodNotSupported(HttpServletRequest req,
                                                            HttpRequestMethodNotSupportedException e) {
        LOG.info("Invalid method {} to URL {}", e.getMethod(), req.getRequestURI());
        
        return new ResponseEntity<>(new ErrorResponse(
                "The method used is not supported for this endpoint",
                "Use one of the supported methods."
        ), HttpStatus.METHOD_NOT_ALLOWED);
    }
    
    // "unwrap" InvocationTargetException since that contains exceptions a lot of the time
    @ExceptionHandler(InvocationTargetException.class)
    public ResponseEntity<ErrorResponse> unwrapReflectionError(HttpServletRequest req, InvocationTargetException e) throws Throwable {
        LOG.info("Unwrapping exception {}", e);
        throw e.getTargetException();
    }
    
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> defaultErrorHandler(HttpServletRequest req, Exception e) {
        ResponseStatus annotation = AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class);
        if (annotation != null) {
            LOG.error("Annotated exception occurred", e);
            
            return new ResponseEntity<>(
                    new ErrorResponse(annotation.reason(), null),
                    annotation.code()
            );
        } else {
            LOG.error("An unknown exception occurred", e);
    
            return new ResponseEntity<>(new InternalServerError(
                    "An unknown exception occurred"
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
