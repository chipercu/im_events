package com.infomaximum.im_event.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * Created by a.kiperku
 * Date: 22.07.2023
 */

@Configuration
public class SecurityConfig extends WebMvcConfigurationSupport  {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://192.168.89.115:8180")
                .allowedMethods("*");
    }
}
