package io.github.anki.anki.configuration


import configuration.learnConfiguration.FileDefaultTemplateBeanPostProcessor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.FileInputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible


@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class FileDefaultTemplateBeanFactoryPostProcessor : BeanFactoryPostProcessor {

    private fun castValueToPropertyType(value: String, targetType: kotlin.reflect.KType): Any? {
    val classifier = targetType.classifier as? kotlin.reflect.KClass<*>
        ?: throw IllegalArgumentException("Unsupported type: $targetType")

    return when (classifier) {
        Int::class -> value.toInt()
        String::class -> value
        ArrayList::class -> value.trim('(', ')').split(", ").toList()
        else -> throw IllegalArgumentException("Unsupported type: $classifier")
    }
}

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val beansMap = beanFactory.getBeansWithAnnotation(FileDefaultTemplate::class.java)
        beansMap.forEach { (beanName, beanInstance) ->
            val annotation = beanInstance::class.findAnnotation<FileDefaultTemplate>()
            if (annotation != null) {
                val properties = Properties()
                FileInputStream(annotation.path).use { properties.load(it) }

                properties.forEach { (key, value) ->
                    val propertyName = key.toString()
                    val propertyValue = value.toString()

                    val property = beanInstance::class.memberProperties.find { it.name == propertyName }
                    property?.let {
                        it.isAccessible = true
                        val castedValue = castValueToPropertyType(propertyValue, it.returnType)
                            (it as? kotlin.reflect.KMutableProperty<*>)?.setter?.call(beanInstance, castedValue)
                    }
                }
            }

        }
    }

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(FileDefaultTemplateBeanPostProcessor::class.java)
    }


}