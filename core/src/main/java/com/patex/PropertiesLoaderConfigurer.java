package com.patex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.StandardServletEnvironment;

import java.util.Map;

@Component
@Profile("trace")
public class PropertiesLoaderConfigurer
        extends PropertySourcesPlaceholderConfigurer {

    private static final String ENVIRONMENT_PROPERTIES = "environmentProperties";

    private static Logger log = LoggerFactory.getLogger(PropertiesLoaderConfigurer.class);

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        super.postProcessBeanFactory(beanFactory);

        final StandardServletEnvironment propertySources =
                (StandardServletEnvironment) super.getAppliedPropertySources().get(ENVIRONMENT_PROPERTIES).getSource();

        propertySources.getPropertySources().forEach(propertySource -> {
            if (propertySource.getSource() instanceof Map) {
                // it will print systemProperties, systemEnvironment, application.properties and other overrides of
                // application.properties
                log.trace("#######" + propertySource.getName() + "#######");


                //noinspection unchecked
                ((Map) propertySource.getSource()).forEach((key, value) -> log.trace(key+"="+value));
            }
        });
    }


}