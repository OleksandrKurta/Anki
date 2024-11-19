package io.github.anki.anki.configuration

import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

@Order(Ordered.HIGHEST_PRECEDENCE)
class LearnScopeBeanFactoryPostProcessor : BeanFactoryPostProcessor {

    override fun postProcessBeanFactory(factory: ConfigurableListableBeanFactory) {
        factory.registerScope("learnScope", LearnScope())
    }
}