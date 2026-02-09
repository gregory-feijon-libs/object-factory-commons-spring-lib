/**
 * Root package for the Object Factory Commons Spring Library.
 * <p>
 * This library provides reusable Java utilities designed to support common application tasks,
 * including reflection operations, enum utilities, and Spring bean factory access.
 * <p>
 * <strong>Main Features:</strong>
 * <ul>
 *   <li>{@link io.github.gregoryfeijon.object.factory.commons.utils.ReflectionUtil ReflectionUtil} -
 *       Utilities for reflection operations including property copying, getter/setter discovery,
 *       and object comparison</li>
 *   <li>{@link io.github.gregoryfeijon.object.factory.commons.utils.ReflectionTypeUtil ReflectionTypeUtil} -
 *       Type inspection utilities with caching for improved performance</li>
 *   <li>{@link io.github.gregoryfeijon.object.factory.commons.utils.FieldUtil FieldUtil} -
 *       Field access utilities with VarHandle support and fallback strategies</li>
 *   <li>{@link io.github.gregoryfeijon.object.factory.commons.utils.enums.EnumUtil EnumUtil} -
 *       Enum utilities for finding constants by property values or name</li>
 *   <li>{@link io.github.gregoryfeijon.object.factory.commons.utils.factory.FactoryUtil FactoryUtil} -
 *       Spring ApplicationContext accessor for programmatic bean retrieval</li>
 * </ul>
 * <p>
 * <strong>Spring Boot Integration:</strong>
 * <p>
 * This library provides auto-configuration for Spring Boot applications via
 * {@link io.github.gregoryfeijon.object.factory.commons.config.ObjectFactoryCommonsAutoConfiguration}.
 * Simply include the dependency in your project, and the utilities will be automatically configured.
 *
 * @author Gregory Maximiano Feijon
 * @since 1.0
 * @see io.github.gregoryfeijon.object.factory.commons.utils
 * @see io.github.gregoryfeijon.object.factory.commons.config
 */
package io.github.gregoryfeijon.object.factory.commons;
