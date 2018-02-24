package com.ming.shopping.beauty;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.web.filter.RequestContextFilter;

import javax.servlet.ServletContext;

/**
 * @author CJ
 */
public class SecurityWebApplicationInitializer extends AbstractSecurityWebApplicationInitializer {
    @Override
    protected void beforeSpringSecurityFilterChain(ServletContext servletContext) {
        super.beforeSpringSecurityFilterChain(servletContext);
        insertFilters(servletContext, new RequestContextFilter());
    }
}
