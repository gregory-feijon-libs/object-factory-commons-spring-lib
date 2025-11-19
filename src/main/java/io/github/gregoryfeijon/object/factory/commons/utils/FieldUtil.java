package io.github.gregoryfeijon.object.factory.commons.utils;

import io.github.gregoryfeijon.object.factory.commons.exception.ApiException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.function.Supplier;

/**
 * Utility class for working with fields using reflection.
 * <p>
 * This class provides methods to get and set field values, even for
 * protected or private fields, using various reflection techniques.
 * <p>
 * <strong>Strategy Pattern:</strong> Uses a fallback chain of access strategies:
 * <ol>
 *   <li>Setter/Getter methods (preferred - respects encapsulation)</li>
 *   <li>VarHandle API (Java 9+ - better performance)</li>
 *   <li>Apache Commons FieldUtils (fallback - maximum compatibility)</li>
 * </ol>
 *
 * @author gregory.feijon
 * @since 1.0
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FieldUtil {

    /**
     * Sets the value of a field, even if it is protected or private.
     * <p>
     * This method tries multiple approaches to set the field value:
     * <ol>
     *   <li>First tries to use a setter method (respects encapsulation)</li>
     *   <li>Then tries to use the VarHandle API (Java 9+, better performance)</li>
     *   <li>Finally falls back to Apache Commons FieldUtils (maximum compatibility)</li>
     * </ol>
     *
     * @param <T>         The type of the object containing the field
     * @param destField   The field to set (must not be null)
     * @param dest        The object containing the field (must not be null)
     * @param sourceValue The value to set (can be null)
     * @throws ApiException If the field value cannot be set by any method
     */
    public static <T> void setProtectedFieldValue(Field destField, T dest, Object sourceValue) {
        validateSetterParameters(destField, dest);
        try {
            setValueUsingSetter(destField, dest, sourceValue);
        } catch (Exception e) {
            try {
                setFieldValueWithHandles(destField, dest, sourceValue);
            } catch (Exception ex) {
                try {
                    setValueUsingFieldUtils(destField, dest, sourceValue);
                } catch (IllegalAccessException exc) {
                    throw new ApiException(
                            String.format("Failed to set value for field '%s' after trying all strategies",
                                    destField.getName()),
                            exc);
                }
            }
        }
    }

    /**
     * Gets the value of a field, even if it is protected or private.
     * <p>
     * This method tries multiple approaches to get the field value:
     * <ol>
     *   <li>First tries to use a getter method (respects encapsulation)</li>
     *   <li>Then tries to use the VarHandle API (Java 9+, better performance)</li>
     *   <li>Finally falls back to Apache Commons FieldUtils (maximum compatibility)</li>
     * </ol>
     *
     * @param field  The field to get (must not be null)
     * @param target The object containing the field (must not be null)
     * @return The value of the field (can be null)
     * @throws ApiException If the field value cannot be retrieved by any method
     */
    public static Object getProtectedFieldValue(Field field, Object target) {
        validateGetterParameters(field, target);
        try {
            return getValueUsingGetter(field, target);
        } catch (Exception e) {
            try {
                return getFieldValueWithHandles(field, target);
            } catch (Exception ex) {
                try {
                    return getValueUsingFieldUtils(field, target);
                } catch (IllegalAccessException exc) {
                    throw new ApiException(
                            String.format("Failed to get value from field '%s' after trying all strategies",
                                    field.getName()),
                            exc);
                }
            }
        }
    }

    /**
     * Checks if the value returned by a supplier is null.
     * <p>
     * This method is a convenience wrapper for null checking, useful for
     * method references and lambda expressions.
     *
     * @param <T>            The type of the value
     * @param getterValidate A supplier that provides the value to check
     * @return true if the value is null, false otherwise
     * @throws ApiException if the supplier itself is null
     */
    public static <T> boolean verifyNull(Supplier<T> getterValidate) {
        if (getterValidate == null) {
            throw new ApiException("Supplier cannot be null");
        }
        T value = getterValidate.get();
        return value == null;
    }

    /**
     * Sets a field value using the VarHandle API.
     * <p>
     * This method uses Java's VarHandle API (Java 9+) to set the value of a field.
     * VarHandle provides better performance than traditional reflection while maintaining
     * type safety and memory ordering guarantees.
     * <p>
     * <strong>Benefits over Field.setAccessible():</strong>
     * <ul>
     *   <li>Better performance (no security checks on every access)</li>
     *   <li>Respects memory ordering (volatile semantics when needed)</li>
     *   <li>Type-safe operations</li>
     * </ul>
     *
     * @param <T>    The type of the object containing the field
     * @param field  The field to set
     * @param target The object containing the field
     * @param value  The value to set
     * @throws ApiException If the field value cannot be set using VarHandle
     */
    private static <T> void setFieldValueWithHandles(Field field, T target, Object value) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(
                    target.getClass(),
                    MethodHandles.lookup());
            VarHandle varHandle = lookup.unreflectVarHandle(field);
            varHandle.set(target, value);
        } catch (Exception ex) {
            throw new ApiException(
                    String.format("VarHandle strategy failed for field '%s'", field.getName()),
                    ex);
        }
    }

    /**
     * Gets a field value using the VarHandle API.
     * <p>
     * This method uses Java's VarHandle API (Java 9+) to get the value of a field.
     * VarHandle provides better performance than traditional reflection.
     *
     * @param field  The field to get
     * @param target The object containing the field
     * @return The value of the field
     * @throws ApiException If the field value cannot be retrieved using VarHandle
     */
    private static Object getFieldValueWithHandles(Field field, Object target) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(
                    target.getClass(),
                    MethodHandles.lookup());
            VarHandle varHandle = lookup.unreflectVarHandle(field);
            return varHandle.get(target);
        } catch (Exception ex) {
            throw new ApiException(
                    String.format("VarHandle strategy failed for field '%s'", field.getName()),
                    ex);
        }
    }

    /**
     * Sets a field value using its setter method.
     * <p>
     * This is the preferred strategy as it respects the class's encapsulation
     * and any business logic in the setter.
     *
     * @param <T>   The type of the object containing the field
     * @param field The field to set
     * @param dest  The object containing the field
     * @param value The value to set
     * @throws Exception If no setter is found or setter invocation fails
     */
    private static <T> void setValueUsingSetter(Field field, T dest, Object value) throws Exception {
        ReflectionUtil.setValueDynamicallyThroughSetterNameFromField(field, dest, value);
    }

    /**
     * Gets a field value using its getter method.
     * <p>
     * This is the preferred strategy as it respects the class's encapsulation
     * and any business logic in the getter.
     *
     * @param field  The field to get
     * @param target The object containing the field
     * @return The value from the getter
     * @throws Exception If no getter is found or getter invocation fails
     */
    private static Object getValueUsingGetter(Field field, Object target) throws Exception {
        return ReflectionUtil.getValueDynamicallyThroughGetterNameFromField(field, target);
    }

    /**
     * Sets a field value using Apache Commons FieldUtils.
     * <p>
     * This is the fallback strategy that uses Apache Commons Lang reflection utilities.
     * It's the most compatible but also the slowest approach.
     *
     * @param <T>   The type of the object containing the field
     * @param field The field to set
     * @param dest  The object containing the field
     * @param value The value to set
     * @throws IllegalAccessException If field access fails
     */
    private static <T> void setValueUsingFieldUtils(Field field, T dest, Object value)
            throws IllegalAccessException {
        FieldUtils.writeField(dest, field.getName(), value, true);
    }

    /**
     * Gets a field value using Apache Commons FieldUtils.
     * <p>
     * This is the fallback strategy that uses Apache Commons Lang reflection utilities.
     *
     * @param field  The field to get
     * @param target The object containing the field
     * @return The value of the field
     * @throws IllegalAccessException If field access fails
     */
    private static Object getValueUsingFieldUtils(Field field, Object target)
            throws IllegalAccessException {
        return FieldUtils.readField(field, target, true);
    }

    /**
     * Validates parameters for setter operations.
     *
     * @param field The field to validate
     * @param dest  The destination object to validate
     * @param <T>   The type of the destination object
     * @throws ApiException if any parameter is null
     */
    private static <T> void validateSetterParameters(Field field, T dest) {
        if (field == null) {
            throw new ApiException("Field cannot be null");
        }
        if (dest == null) {
            throw new ApiException("Destination object cannot be null");
        }
    }

    /**
     * Validates parameters for getter operations.
     *
     * @param field  The field to validate
     * @param target The target object to validate
     * @throws ApiException if any parameter is null
     */
    private static void validateGetterParameters(Field field, Object target) {
        if (field == null) {
            throw new ApiException("Field cannot be null");
        }
        if (target == null) {
            throw new ApiException("Target object cannot be null");
        }
    }
}