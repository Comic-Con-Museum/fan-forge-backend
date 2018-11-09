package org.comic_con.museum.fcb.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
@Order(Integer.MIN_VALUE)
public class RequestIDFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger("filter.req_id");
    
    @Override
    public void init(FilterConfig filterConfig) {
        // No setup needed
    }
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain next) throws IOException, ServletException {
        if (req instanceof HttpServletRequest && res instanceof HttpServletResponse) {
            MDC.put("request", UUID.randomUUID().toString());
            HttpServletRequest hreq = (HttpServletRequest) req;
            LOG.info("Added request ID to {} {}", hreq.getMethod(), hreq.getRequestURI());
        }
        next.doFilter(req, res);
    }

    @Override
    public void destroy() {
        // No destruction needed
    }
}
