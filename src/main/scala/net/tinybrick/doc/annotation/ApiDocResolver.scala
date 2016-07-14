package net.tinybrick.doc.annotation

/**
  * Created by wangji on 2016/6/14.
  */

import org.apache.log4j.Logger
import org.springframework.beans.BeansException
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.context.WebApplicationContext
import springfox.documentation.builders.PathSelectors.regex
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.util._

class ApiDocResolver extends ApplicationContextAware with BeanFactoryAware with InitializingBean{
    private var context: WebApplicationContext = null
    private var beanFactory: DefaultListableBeanFactory = null
    private[annotation] var logger: Logger = Logger.getLogger(classOf[ApiDocResolver])

    private def getAnnotationField(annotation: Annotation, field: String): String ={
        val `type`: Class[_ <: Annotation] = annotation.annotationType
        try {
            val method: Method = `type`.getDeclaredMethod(field)
            val value:String  = method.invoke(annotation) match {
                case v: String => v.length match {
                    case 0 => null
                    case _ => v
                }
                case values:Array[String] => values(0).length match {
                    case 0 => null
                    case _ => values(0)
                }
            }

            return value
        }
        catch {
            case e: Exception => {
                logger.warn(e.getMessage, e)
            }
        }
        return null
    }

    private def getGroupName(annotation: Annotation): String = {
        val field = "value"
        return getAnnotationField(annotation, field)
    }


    private def getPath(annotation: Annotation): String = {
        val field = "path"
        getAnnotationField(annotation, field)
    }

    private def getUri(annotation: Annotation): String = {
        val field = "value"
        getAnnotationField(annotation, field)
    }

    private def addPath(pathsMap: Map[String, List[String]], groupName: String, path: String) {
        if (groupName.length > 0) {
            var paths: List[String] = null
            if (pathsMap.containsKey(groupName)) paths = pathsMap.get(groupName)
            else paths = new ArrayList[String]
            paths.add(path)
            pathsMap.put(groupName, paths)
        }
    }

    @throws(classOf[Exception])
    def afterPropertiesSet {
        val pathsMap: Map[String, List[String]] = new HashMap[String, List[String]]
        val objectMap: Map[String, AnyRef] = context.getBeansWithAnnotation(classOf[ApiDoc])
        import scala.collection.JavaConversions._
        for (myBean <- objectMap.values) {
            val clazz: Class[_ <: AnyRef] = myBean.getClass
            if (clazz.isAnnotationPresent(classOf[ApiDoc])) {
                val annotation: Annotation = clazz.getAnnotation(classOf[ApiDoc])
                val groupName: String = getGroupName(annotation)
                val path: String = getPath(annotation) match {
                    case p: String => p
                    case null => {
                        if (clazz.isAnnotationPresent(classOf[RequestMapping]))
                            getUri(clazz.getAnnotation(classOf[RequestMapping]))
                        else
                            null
                    }
                }
                addPath(pathsMap, groupName, path)
            }
        }
        registerDocket(pathsMap)
    }

    private def registerDocket(pathsMap: Map[String, List[String]]) {
        import scala.collection.JavaConversions._
        for (group <- pathsMap.keySet) {
            val paths: List[String] = pathsMap.get(group)
            val regExp: String = generatePathRegExp(paths)
            val docket: Docket = createDocket(regExp, group, null, null, null)
            beanFactory.registerSingleton(classOf[Docket].getSimpleName + "_" + group, docket)
        }
    }

    private def generatePathRegExp(paths: List[String]): String = {
        var regExp: String = null
        if (paths.size > 1) {
            val iter: Iterator[String] = paths.iterator
            while (iter.hasNext) {
                if (null == regExp) regExp = "[" + iter.next
                else regExp += "|" + iter.next
            }
            regExp += "]+/.*"
        }
        else regExp = paths.get(0) + "/.*"
        return regExp
    }

    def createDocket(paths: String, apiGrougName: String, apiTitle: String, apiDescription: String, version: String): Docket = {
        val apiInfo: ApiInfo = new ApiInfo(apiTitle, apiDescription, version, null, new Contact(null, null, null), null, null)
        val docket: Docket = new Docket(DocumentationType.SWAGGER_2)
            .select
            .paths(regex(paths))
            .build
            .apiInfo(apiInfo)
            .useDefaultResponseMessages(false)
        docket.groupName(if (null == apiGrougName) null else apiGrougName)
        return docket
    }

    def setApplicationContext(applicationContext: ApplicationContext) {
        this.context = applicationContext.asInstanceOf[WebApplicationContext]
    }

    @throws(classOf[BeansException])
    def setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory.asInstanceOf[DefaultListableBeanFactory]
    }
}
