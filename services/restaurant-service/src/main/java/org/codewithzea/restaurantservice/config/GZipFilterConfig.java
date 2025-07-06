package org.codewithzea.restaurantservice.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GZipFilterConfig {
    @Bean
    public FilterRegistrationBean<GZipServletFilter> gzipFilterRegistration() {
        FilterRegistrationBean<GZipServletFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new GZipServletFilter());
        registration.addUrlPatterns("/*");
        registration.setName("gzipFilter");
        registration.setOrder(1);
        return registration;
    }
}
