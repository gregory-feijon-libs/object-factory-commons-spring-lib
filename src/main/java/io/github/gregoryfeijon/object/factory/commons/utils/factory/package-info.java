/**
 * Spring bean factory utilities for the Object Factory Commons library.
 * <p>
 * This package provides utilities for programmatic access to Spring beans
 * outside of Spring-managed components.
 * <p>
 * <strong>Available Utilities:</strong>
 * <ul>
 *   <li>{@link io.github.gregoryfeijon.object.factory.commons.utils.factory.FactoryUtil FactoryUtil} -
 *       Spring ApplicationContext accessor providing:
 *       <ul>
 *         <li>Bean retrieval by class type</li>
 *         <li>Bean retrieval by name and type</li>
 *         <li>Optional-returning methods for null-safe access</li>
 *         <li>Context initialization status check</li>
 *       </ul>
 *   </li>
 * </ul>
 * <p>
 * <strong>Usage Example:</strong>
 * <pre>{@code
 * // Get a bean by type
 * MyService service = FactoryUtil.getBean(MyService.class);
 *
 * // Get a bean by name and type
 * MyService namedService = FactoryUtil.getBeanFromName("myServiceImpl", MyService.class);
 *
 * // Optional-returning version for null-safe access
 * Optional<MyService> optionalService = FactoryUtil.getBeanOptional(MyService.class);
 *
 * // Check if context is initialized
 * if (FactoryUtil.isContextInitialized()) {
 *     // Safe to access beans
 * }
 * }</pre>
 * <p>
 * <strong>Note:</strong>
 * <p>
 * The ApplicationContext is automatically set by the auto-configuration.
 * Beans can only be retrieved after the Spring context is fully initialized.
 *
 * @author Gregory Maximiano Feijon
 * @since 1.0
 * @see io.github.gregoryfeijon.object.factory.commons.utils.factory.FactoryUtil
 */
package io.github.gregoryfeijon.object.factory.commons.utils.factory;
