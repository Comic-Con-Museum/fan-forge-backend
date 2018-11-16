package org.comic_con.museum.fcb.endpoints;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;

/**
 * The endpoints all handle normal errors -- no such exhibit, auth
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
    
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> integrityViolation(HttpServletRequest req, DuplicateKeyException e) {
        LOG.info("Tegridy violation: {}", e.getMessage());
        return ResponseEntity.badRequest().build();
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
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> oversizedUpload(HttpServletRequest req, MaxUploadSizeExceededException e) {
        LOG.info("Upload to {} was too large: {} bytes", req.getRequestURI(), req.getContentLengthLong());
        return new ResponseEntity<>(new ErrorResponse(
                "Upload was too large",
                "Limit the upload size; see the docs for the maximum per file and overall"
        ), HttpStatus.PAYLOAD_TOO_LARGE);
    }
    
    @ExceptionHandler(AmazonS3Exception.class)
    public ResponseEntity<ErrorResponse> s3Exception(HttpServletRequest req, AmazonS3Exception e) {
        LOG.info("S3 exception: {}", e.getMessage());
        return new ResponseEntity<>(HttpStatus.valueOf(e.getStatusCode()));
    }
    
    // "unwrap" InvocationTargetException since that's what errors in reflected methods are wrapped in
    @ExceptionHandler(InvocationTargetException.class)
    public ResponseEntity<ErrorResponse> unwrapReflectionError(HttpServletRequest req, InvocationTargetException e) throws Throwable {
        LOG.info("Unwrapping exception {}", e);
        throw e.getTargetException();
    }
    
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> defaultErrorHandler(HttpServletRequest req, Throwable e) {
        ResponseStatus annotation = AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class);
        if (annotation != null) {
            LOG.error("Annotated exception occurred", e);
            
            return new ResponseEntity<>(
                    new ErrorResponse(
                            annotation.reason().isEmpty() ? e.getMessage() : annotation.reason(),
                            null
                    ),
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
