package org.mariaelvin.library.analytics_service.security;

import org.mariaelvin.library.analytics_service.security.JwtAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean =
                new FilterRegistrationBean<>(jwtAuthenticationFilter);

        /*
         * JwtAuthenticationFilter should run only inside Spring Security filter chain
         * through SecurityConfig.addFilterBefore().
         */
        registrationBean.setEnabled(false);

        return registrationBean;
    }
}