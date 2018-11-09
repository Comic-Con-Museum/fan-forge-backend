package org.comic_con.museum.fcb.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// TODO: Add more fine-grained controls, as applicable.
@Component
@Order(Integer.MIN_VALUE + 100)
public class CORSAllowAllFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger("filter.cors");
    
    @Override
    public void init(FilterConfig filterConfig) {
        // No initialization needed
    }
    
    @Override
    public void doFilter(ServletRequest rreq, ServletResponse rres, FilterChain next) throws ServletException,
            IOException {
        if (!(rreq instanceof HttpServletRequest) || !(rres instanceof HttpServletResponse)) {
            LOG.info("Not an HTTP request, ignoring");
            next.doFilter(rreq, rres);
            return;
        }
        HttpServletRequest req = (HttpServletRequest) rreq;
        HttpServletResponse res = (HttpServletResponse) rres;
        LOG.info("Adding CORS info to {} {}", req.getMethod(), req.getRequestURI());
        res.addHeader("Access-Control-Allow-Origin", "*");
        res.addHeader("Access-Control-Allow-Methods", req.getHeader("Access-Control-Request-Method"));
        res.addHeader("Access-Control-Allow-Headers", req.getHeader("Access-Control-Request-Headers"));
        res.addHeader("Access-Control-Max-Age", "-1"); // 86,400 seconds = 1 day
        res.addHeader("Access-Control-Allow-Credentials", "true");
        if (!req.getMethod().equals("OPTIONS")) {
            LOG.info("Not a preflight request, doing the rest of the handling");
            next.doFilter(req, res);
        } else {
            LOG.info("Preflight request, skipping rest of the handling");
            res.setStatus(HttpServletResponse.SC_OK); // must be 200, not 204, even though we don't give any content
        }
    }
    
    @Override
    public void destroy() {
        // Nothing to destroy
    }
}
