/**
 * Core utility classes for the Object Factory Commons library.
 * <p>
 * This package provides reflection and type utilities for common Java operations
 * that are frequently needed in application development.
 * <p>
 * <strong>Available Utilities:</strong>
 * <ul>
 *   <li>{@link io.github.gregoryfeijon.object.factory.commons.utils.ReflectionUtil ReflectionUtil} -
 *       Method/field discovery and safe null-handling for property access:
 *       <ul>
 *         <li>Finding getters/setters/fields on an object's class hierarchy</li>
 *         <li>Safe getter access with default values ({@code safeGet}, {@code safeGetWithDefaultValue})</li>
 *       </ul>
 *   </li>
 *   <li>{@link io.github.gregoryfeijon.object.factory.commons.utils.ReflectionComparator ReflectionComparator} -
 *       Comparing objects by their getter values, with field filtering and
 *       null/empty/zero equivalence rules.
 *   </li>
 *   <li>{@link io.github.gregoryfeijon.object.factory.commons.utils.DynamicAccessorUtil DynamicAccessorUtil} -
 *       Invoking getters/setters dynamically by name, with automatic primitive/wrapper
 *       type conversion.
 *   </li>
 *   <li>{@link io.github.gregoryfeijon.object.factory.commons.utils.PropertyCopier PropertyCopier} -
 *       Copying properties between objects by matching getter/setter names.
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
 * @see io.github.gregoryfeijon.object.factory.commons.utils.ReflectionComparator
 * @see io.github.gregoryfeijon.object.factory.commons.utils.DynamicAccessorUtil
 * @see io.github.gregoryfeijon.object.factory.commons.utils.PropertyCopier
 * @see io.github.gregoryfeijon.object.factory.commons.utils.ReflectionTypeUtil
 * @see io.github.gregoryfeijon.object.factory.commons.utils.FieldUtil
 */
package io.github.gregoryfeijon.object.factory.commons.utils;
