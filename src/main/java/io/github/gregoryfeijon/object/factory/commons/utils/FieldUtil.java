package io.github.gregoryfeijon.object.factory.commons.utils;

import io.github.gregoryfeijon.object.factory.commons.exception.ApiException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.List;
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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FieldUtil {

    /**
     * Functional interface for field setter strategies.
     */
    @FunctionalInterface
    private interface FieldSetterStrategy {
        void setValue(Field field, Object target, Object value) throws ReflectiveOperationException;
    }

    /**
     * Functional interface for field getter strategies.
     */
    @FunctionalInterface
    private interface FieldGetterStrategy {
        Object getValue(Field field, Object target) throws ReflectiveOperationException;
    }

    /**
     * Ordered list of setter strategies to try.
     */
    private static final List<FieldSetterStrategy> SETTER_STRATEGIES = List.of(
            FieldUtil::setValueUsingSetter,
            FieldUtil::setFieldValueWithHandles,
            FieldUtil::setValueUsingFieldUtils
    );

    /**
     * Ordered list of getter strategies to try.
     */
    private static final List<FieldGetterStrategy> GETTER_STRATEGIES = List.of(
            FieldUtil::getValueUsingGetter,
            FieldUtil::getFieldValueWithHandles,
            FieldUtil::getValueUsingFieldUtils
    );

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
        validateFieldParameters(destField, dest, "Destination object");

        Exception lastException = null;
        for (FieldSetterStrategy strategy : SETTER_STRATEGIES) {
            try {
                strategy.setValue(destField, dest, sourceValue);
                return;
            } catch (Exception e) {
                lastException = e;
            }
        }

        throw new ApiException(
                String.format("Failed to set value for field '%s' after trying all strategies",
                        destField.getName()),
                lastException);
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
        validateFieldParameters(field, target, "Target object");

        Exception lastException = null;
        for (FieldGetterStrategy strategy : GETTER_STRATEGIES) {
            try {
                return strategy.getValue(field, target);
            } catch (Exception e) {
                lastException = e;
            }
        }

        throw new ApiException(
                String.format("Failed to get value from field '%s' after trying all strategies",
                        field.getName()),
                lastException);
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

    // ==================== Setter Strategies ====================

    /**
     * Sets a field value using its setter method.
     * <p>
     * This is the preferred strategy as it respects the class's encapsulation
     * and any business logic in the setter.
     *
     * @param field  The field to set
     * @param target The object containing the field
     * @param value  The value to set
     * @throws ApiException If no setter is found or setter invocation fails
     */
    private static void setValueUsingSetter(Field field, Object target, Object value) {
        DynamicAccessorUtil.setValueDynamicallyThroughSetterNameFromField(field, target, value);
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
     * @param field  The field to set
     * @param target The object containing the field
     * @param value  The value to set
     * @throws ReflectiveOperationException If the field value cannot be set using VarHandle
     */
    private static void setFieldValueWithHandles(Field field, Object target, Object value) throws ReflectiveOperationException {
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(
                target.getClass(),
                MethodHandles.lookup());
        VarHandle varHandle = lookup.unreflectVarHandle(field);
        varHandle.set(target, value);
    }

    /**
     * Sets a field value using Apache Commons FieldUtils.
     * <p>
     * This is the fallback strategy that uses Apache Commons Lang reflection utilities.
     * It's the most compatible but also the slowest approach.
     *
     * @param field  The field to set
     * @param target The object containing the field
     * @param value  The value to set
     * @throws ReflectiveOperationException If field access fails
     */
    private static void setValueUsingFieldUtils(Field field, Object target, Object value) throws ReflectiveOperationException {
        FieldUtils.writeField(target, field.getName(), value, true);
    }

    // ==================== Getter Strategies ====================

    /**
     * Gets a field value using its getter method.
     * <p>
     * This is the preferred strategy as it respects the class's encapsulation
     * and any business logic in the getter.
     *
     * @param field  The field to get
     * @param target The object containing the field
     * @return The value from the getter
     * @throws ApiException If no getter is found or getter invocation fails
     */
    private static Object getValueUsingGetter(Field field, Object target) {
        return DynamicAccessorUtil.getValueDynamicallyThroughGetterNameFromField(field, target);
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
     * @throws ReflectiveOperationException If the field value cannot be retrieved using VarHandle
     */
    private static Object getFieldValueWithHandles(Field field, Object target) throws ReflectiveOperationException {
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(
                target.getClass(),
                MethodHandles.lookup());
        VarHandle varHandle = lookup.unreflectVarHandle(field);
        return varHandle.get(target);
    }

    /**
     * Gets a field value using Apache Commons FieldUtils.
     * <p>
     * This is the fallback strategy that uses Apache Commons Lang reflection utilities.
     *
     * @param field  The field to get
     * @param target The object containing the field
     * @return The value of the field
     * @throws ReflectiveOperationException If field access fails
     */
    private static Object getValueUsingFieldUtils(Field field, Object target) throws ReflectiveOperationException {
        return FieldUtils.readField(field, target, true);
    }

    // ==================== Validation ====================

    /**
     * Validates parameters for field operations.
     *
     * @param field       The field to validate
     * @param target      The target object to validate
     * @param targetLabel Label for the target in error messages (e.g., "Destination object", "Target object")
     * @throws ApiException if any parameter is null
     */
    private static void validateFieldParameters(Field field, Object target, String targetLabel) {
        if (field == null) {
            throw new ApiException("Field cannot be null");
        }
        if (target == null) {
            throw new ApiException(targetLabel + " cannot be null");
        }
    }
}
