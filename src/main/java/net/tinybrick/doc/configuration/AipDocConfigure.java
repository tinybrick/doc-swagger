package net.tinybrick.doc.configuration;

/**
 * Created by wangji on 2016/6/14.
 */

import net.tinybrick.doc.annotation.ApiDocResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableAutoConfiguration
@Configuration
@PropertySource (value = { "classpath:config/apidoc.properties"} )
@EnableSwagger2
public class AipDocConfigure extends WebMvcConfigurerAdapter {
    @Bean public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Value("${doc.api.enableUI:true}") boolean enableSwaggerUI = true;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if(enableSwaggerUI) {
            registry.addResourceHandler("swagger-ui.html")
                    .addResourceLocations("classpath:/META-INF/resources/");

            registry.addResourceHandler("/webjars/**")
                    .addResourceLocations("classpath:/META-INF/resources/webjars/");
        }
        super.addResourceHandlers(registry);
    }

    @Bean
    public ApiDocResolver apiDocResolver() {
        return new ApiDocResolver();
    }
}

