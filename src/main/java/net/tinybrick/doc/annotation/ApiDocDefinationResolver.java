package net.tinybrick.doc.annotation;

/**
 * Created by wangji on 2016/6/14.
 */

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import static springfox.documentation.builders.PathSelectors.regex;

public class ApiDocDefinationResolver implements ApplicationContextAware, BeanFactoryAware, InitializingBean {
    private WebApplicationContext context;
    private DefaultListableBeanFactory beanFactory;
    static Logger logger = Logger.getLogger(ApiDocDefinationResolver.class);

    private String getGroupName(Annotation annotation, String field){
        Class<? extends Annotation> type= annotation.annotationType();
        try {
            Method method = type.getDeclaredMethod(field);
            String groupName  = (String) method.invoke(annotation);
            if(groupName.length() > 0) {
                return groupName;
            }
        }
        catch(Exception e){
            logger.warn(e.getMessage(), e);
        }
        
        return null;
    }
    
    private String getPath(Annotation annotation, String field) {
        Class<? extends Annotation> type= annotation.annotationType();
        try {
            Method method = type.getDeclaredMethod(field);
            Object path  =  method.invoke(annotation);
            if(path instanceof String[]){
                if(((String[])path).length > 0)
                    return ((String[])path)[0];
            }
            else {
                if (((String)path).length() > 0) {
                    return (String) path;
                }
            }
        }
        catch(Exception e){
            logger.warn(e.getMessage(), e);
        }

        return null;
    }
    
    private void addPath( Map<String, List<String>> pathsMap, String groupName, String path) {
        if(groupName.length() > 0) {
            List<String> paths = null;
            if(pathsMap.containsKey(groupName))
                paths = pathsMap.get(groupName);
            else
                paths = new ArrayList<String>();
            
            paths.add(path);
            pathsMap.put(groupName, paths);
        }
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, List<String>> pathsMap = new HashMap<String, List<String>>();

        //String rootPath = "/";
        final Map<String, Object> objectMap = context.getBeansWithAnnotation(ApiDocDefination.class);

        for (final Object myBean : objectMap.values()) {
            final Class<? extends Object> clazz = myBean.getClass();

            if(clazz.isAnnotationPresent(ApiDocDefination.class)) {
                Annotation annotation = clazz.getAnnotation(ApiDocDefination.class);
                String groupName = getGroupName(annotation, "value");
                String path = getPath(annotation, "path");
                if(path == null && clazz.isAnnotationPresent(RequestMapping.class))
                    path = getPath(clazz.getAnnotation(RequestMapping.class), "value");
                else {
                    continue;
                }

                addPath(pathsMap, groupName, path);

                //Docket docket = swaggerSpringMvcPlugin(rootPath, null, null, rootPath.substring(1));
                //beanFactory.registerSingleton(rootPath, docket);
            }
        }

        registerDocket(pathsMap);
    }
    
    private void registerDocket(Map<String, List<String>> pathsMap) {
        for (String group : pathsMap.keySet()) {
            List<String> paths = pathsMap.get(group);
            String regExp = generatePathRegExp(paths);
            Docket docket =  createDocket(regExp, group, null, null, null);
            beanFactory.registerSingleton(Docket.class.getSimpleName() + "_" + group, docket);
        }
    }

    private String generatePathRegExp(List<String> paths){
        String regExp = null;
        if (paths.size() > 1) {
            Iterator<String> iter = paths.iterator();
            while(iter.hasNext()) {
                if(null == regExp)
                    regExp  = "[" + iter.next();
                else
                    regExp += "|" + iter.next();
            }
            regExp += "]+/.*";
        }
        else
            regExp = paths.get(0) + "/.*";

        return regExp;
    }

    public Docket createDocket(String paths, String apiGrougName, String apiTitle, String apiDescription, String version) {
        ApiInfo apiInfo = new ApiInfo(apiTitle, apiDescription, version, null, new Contact(null,null,null), null, null);
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .select()
                .paths(regex(paths))
                .build()
                .apiInfo(apiInfo)
                .useDefaultResponseMessages(false);

        docket.groupName(null==apiGrougName?null:apiGrougName);

        return docket;
    }

    /*Predicate<String> includePath(final List<String> path) {
        return new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return path.contains(input);
            }
        };
    }*/

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.context = (WebApplicationContext) applicationContext;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }
}