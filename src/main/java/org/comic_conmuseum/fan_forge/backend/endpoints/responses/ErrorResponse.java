package org.comic_conmuseum.fan_forge.backend.endpoints.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.MDC;

public class ErrorResponse {
    private String error;
    private String fix;
    private String requestId;
    
    public ErrorResponse(String error, String fix) {
        this.error = error;
        this.fix = fix;
        this.requestId = MDC.get("request");
    }

    public String getError() { return error; }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getFix() { return fix; }
    public String getCode() { return requestId; }
}
