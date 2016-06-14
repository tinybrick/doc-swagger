package net.tinybrick.doc.configuration;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static springfox.documentation.builders.PathSelectors.regex;

/**
 * Created by wangji on 2016/6/14.
 */
public class AutoApiDocEnabler {
    static Logger logger = Logger.getLogger(AutoApiDocEnabler.class);

    String rootPath = "/";
    String apiTitle = null;
    String apiDescription = "";
    String apiGrougName = null;

    public void setApiTitle(String apiTitle) {
        this.apiTitle = apiTitle;
    }

    public void setApiDescription(String apiDescription) {
        this.apiDescription = apiDescription;
    }

    public void setApiGrougName(String apiGrougName) {
        this.apiGrougName = apiGrougName;
    }

    public AutoApiDocEnabler() {
        Class clazz = this.getClass();
        apiTitle = (null == apiTitle? clazz.getSimpleName() + " API":null);

        if(clazz.isAnnotationPresent(RequestMapping.class)) {
            Annotation annotation = clazz.getAnnotation(RequestMapping.class);
            Class<? extends Annotation> type= annotation.annotationType();
            try {
                Method method = type.getDeclaredMethod("value");
                String[] values  = (String[]) method.invoke(annotation);
                if(values.length > 0)
                    rootPath = values[0];
            }
            catch(Exception e){
                logger.warn(e.getMessage(), e);
            }
        }
    }

    //@Bean
    public Docket swaggerSpringMvcPlugin() {
        ApiInfo apiInfo = new ApiInfo(apiTitle, apiDescription, rootPath.toLowerCase().matches("^\\/[v|ver]+[0-9.]+")?rootPath.substring(1):null, null, new Contact(null,null,null), null, null);
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .select()
                .paths(regex(rootPath + "/.*"))
                .build()
                .apiInfo(apiInfo)
                .useDefaultResponseMessages(false);

        docket.groupName(null==apiGrougName?null:apiGrougName);

        return docket;
    }
}
