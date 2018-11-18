package org.comic_conmuseum.fan_forge.backend.filters;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Order(Integer.MIN_VALUE + 1) // happen just after the request ID generator
public class RequestLoggingFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger("filter.req_log");
    
    @Override
    public void init(FilterConfig filterConfig) {
        // no init needed
    }

    @Override
    public void doFilter(ServletRequest rawReq, ServletResponse rawRes, FilterChain next) throws IOException, ServletException {
        if (!(rawReq instanceof HttpServletRequest) || !(rawRes instanceof HttpServletResponse)) {
            next.doFilter(rawReq, rawRes);
            return;
        }
        HttpServletRequest req = (HttpServletRequest) rawReq;
        LOG.info("Answering {} {}", req.getMethod(), req.getRequestURI());
        HttpServletResponse res = (HttpServletResponse) rawRes;
        ContentCachingResponseWrapper caching = new ContentCachingResponseWrapper(res);
    
        next.doFilter(req, caching);
    
        if (caching.getContentType() == null) {
            LOG.info("Responded {} with no set content type", caching.getStatusCode());
        } else if (
                caching.getContentType().startsWith("application/json") ||
                caching.getContentType().startsWith("application/xml") ||
                caching.getContentType().startsWith("application/xhtml") ||
                caching.getContentType().startsWith("text/")
        ) {
            LOG.info("Responded {} with body of type {}: {}",
                    caching.getStatusCode(),
                    caching.getContentType(),
                    new String(caching.getContentAsByteArray(), StandardCharsets.UTF_8)
            );
        } else {
            LOG.info("Responded {} with non-text body of type {}",
                    caching.getStatusCode(),
                    caching.getContentType()
            );
            // manually check to avoid the hard work of Hex.encodeHexString unless
            //  it's absolutely necessary
            if (LOG.isTraceEnabled()) {
                LOG.trace("Body hex: {}", Hex.encodeHexString(caching.getContentAsByteArray()));
            }
        }
        
        caching.copyBodyToResponse();
    }

    @Override
    public void destroy() {
        // no destroy needed
    }
}
