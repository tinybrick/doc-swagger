package net.tinybrick.doc.configuration

import net.tinybrick.doc.annotation.ApiDocResolver
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{Configuration, PropertySource, Bean}
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.web.servlet.config.annotation.{WebMvcConfigurerAdapter, ResourceHandlerRegistry}
import springfox.documentation.swagger2.annotations.EnableSwagger2

/**
  * Created by wangji on 2016/6/15.
  */
@EnableAutoConfiguration
@Configuration
@PropertySource(value = Array("classpath:config/apidoc.properties"))
@EnableSwagger2
class AipDocConfigure extends WebMvcConfigurerAdapter{
    @Bean def propertySourcesPlaceholderConfigurer: PropertySourcesPlaceholderConfigurer = {
        return new PropertySourcesPlaceholderConfigurer
    }

    @Value("${doc.api.enableUI:true}") private[configuration] var enableSwaggerUI: Boolean = true

    override def addResourceHandlers(registry: ResourceHandlerRegistry) {
        if (enableSwaggerUI) {
            registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/")
            registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/")
        }
        super.addResourceHandlers(registry)
    }

    @Bean def apiDocResolver: ApiDocResolver = {
        return new ApiDocResolver
    }
}
