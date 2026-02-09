/**
 * Core utility classes for the Object Factory Commons library.
 * <p>
 * This package provides reflection and type utilities for common Java operations
 * that are frequently needed in application development.
 * <p>
 * <strong>Available Utilities:</strong>
 * <ul>
 *   <li>{@link io.github.gregoryfeijon.object.factory.commons.utils.ReflectionUtil ReflectionUtil} -
 *       Comprehensive reflection utilities including:
 *       <ul>
 *         <li>Finding and invoking getters/setters dynamically</li>
 *         <li>Comparing objects by their property values</li>
 *         <li>Copying properties between objects</li>
 *         <li>Safe null-handling for property access</li>
 *       </ul>
 *   </li>
 *   <li>{@link io.github.gregoryfeijon.object.factory.commons.utils.ReflectionTypeUtil ReflectionTypeUtil} -
 *       Type inspection utilities including:
 *       <ul>
 *         <li>Resolving generic types (ParameterizedType, GenericArrayType, etc.)</li>
 *         <li>Detecting primitives, wrappers, collections, and maps</li>
 *         <li>Java Records support (Java 16+)</li>
 *         <li>Thread-safe caching for improved performance</li>
 *       </ul>
 *   </li>
 *   <li>{@link io.github.gregoryfeijon.object.factory.commons.utils.FieldUtil FieldUtil} -
 *       Protected field access utilities using:
 *       <ul>
 *         <li>VarHandle API for optimal performance (Java 9+)</li>
 *         <li>Strategy pattern with fallback chain</li>
 *         <li>Thread-safe field access</li>
 *       </ul>
 *   </li>
 * </ul>
 * <p>
 * <strong>Thread Safety:</strong>
 * <p>
 * All utilities in this package are designed to be thread-safe. Internal caches use
 * {@link java.util.concurrent.ConcurrentHashMap} to ensure safe concurrent access.
 * <p>
 * <strong>Performance:</strong>
 * <p>
 * Method and type resolution results are cached to avoid repeated reflection overhead.
 * First calls may take ~100µs while cached calls are ~10ns (10,000x faster).
 *
 * @author Gregory Maximiano Feijon
 * @since 1.0
 * @see io.github.gregoryfeijon.object.factory.commons.utils.ReflectionUtil
 * @see io.github.gregoryfeijon.object.factory.commons.utils.ReflectionTypeUtil
 * @see io.github.gregoryfeijon.object.factory.commons.utils.FieldUtil
 */
package io.github.gregoryfeijon.object.factory.commons.utils;
