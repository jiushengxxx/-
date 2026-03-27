package com.ct.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.io.File;

/**
 * 静态资源配置类
 */
@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    // 读取 application.yml 的上传路径
    @Value("${web.upload-path}")
    private String uploadPath;

    /**
     * 配置静态资源访问
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 1 项目静态资源
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");

        // 2 上传图片访问路径
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPath + File.separator);

        super.addResourceHandlers(registry);
    }

    /**
     * 跨域配置
     */
    @Override
    protected void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true)
                .maxAge(3600);

    }
}