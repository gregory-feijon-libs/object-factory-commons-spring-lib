package io.github.gregoryfeijon.object.factory.commons.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for Object Factory Commons.
 * <p>
 * This configuration is automatically loaded by Spring Boot when the library
 * is present on the classpath. It registers all components including utilities,
 * services, and factory classes.
 * </p>
 * <p>
 * To disable this auto-configuration, add to your application.properties:
 * </p>
 * <pre>
 * spring.autoconfigure.exclude=io.github.gregoryfeijon.object.factory.config.ObjectFactoryCommonsAutoConfiguration
 * </pre>
 *
 * @see io.github.gregoryfeijon.object.factory.commons.utils.factory.FactoryUtil
 * @author Gregory Feijon
 * @since 1.0.0
 */
@AutoConfiguration
@ComponentScan(basePackages = "io.github.gregoryfeijon.object.factory.commons")
public class ObjectFactoryCommonsAutoConfiguration {
}
