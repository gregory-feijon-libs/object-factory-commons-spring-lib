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
 *
 * @author gregory.feijon
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FieldUtil {

    /**
     * Sets the value of a field, even if it is protected or private.
     * <p>
     * This method tries multiple approaches to set the field value:
     * <ol>
     *   <li>First tries to use a setter method</li>
     *   <li>Then tries to use the VarHandle API</li>
     *   <li>Finally falls back to Apache Commons FieldUtils</li>
     * </ol>
     *
     * @param <T>         The type of the object containing the field
     * @param destField   The field to set
     * @param dest        The object containing the field
     * @param sourceValue The value to set
     * @throws ApiException If the field value cannot be set by any method
     */
    public static <T> void setProtectedFieldValue(Field destField, T dest, Object sourceValue) {
        try {
            ReflectionUtil.setValueDynamicallyThroughSetterNameFromField(destField, dest, sourceValue);
        } catch (Exception e) {
            try {
                setFieldValueWithHandles(destField, dest, sourceValue);
            } catch (Exception ex) {
                try {
                    FieldUtils.writeField(dest, destField.getName(), sourceValue, true);
                } catch (IllegalAccessException exc) {
                    throw new ApiException("Error setting value for field " + destField.getName(), ex);
                }
            }
        }
    }

    /**
     * Sets a field value using the VarHandle API.
     * <p>
     * This method uses Java's VarHandle API to set the value of a field,
     * which can provide better performance than traditional reflection.
     *
     * @param <T>    The type of the object containing the field
     * @param field  The field to set
     * @param target The object containing the field
     * @param value  The value to set
     * @throws ApiException If the field value cannot be set using VarHandle
     */
    private static <T> void setFieldValueWithHandles(Field field, T target, Object value) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(target.getClass(), MethodHandles.lookup());
            VarHandle varHandle = lookup.unreflectVarHandle(field);
            varHandle.set(target, value);
        } catch (Exception ex) {
            throw new ApiException("Error setting value via VarHandle for field: " + field.getName(), ex);
        }
    }

    /**
     * Gets the value of a field, even if it is protected or private.
     * <p>
     * This method tries multiple approaches to get the field value:
     * <ol>
     *   <li>First tries to use a getter method</li>
     *   <li>Then tries to use the VarHandle API</li>
     *   <li>Finally falls back to Apache Commons FieldUtils</li>
     * </ol>
     *
     * @param field  The field to get
     * @param target The object containing the field
     * @return The value of the field
     * @throws ApiException If the field value cannot be retrieved by any method
     */
    public static Object getProtectedFieldValue(Field field, Object target) {
        try {
            return ReflectionUtil.getValueDynamicallyThroughGetterNameFromField(field, target);
        } catch (Exception e) {
            try {
                return getFieldValueWithHandles(field, target);
            } catch (Exception ex) {
                try {
                    return FieldUtils.readField(field, target, true);
                } catch (IllegalAccessException exc) {
                    throw new ApiException("Error trying to get value from field " + field.getName(), ex);
                }
            }
        }
    }

    /**
     * Gets a field value using the VarHandle API.
     * <p>
     * This method uses Java's VarHandle API to get the value of a field,
     * which can provide better performance than traditional reflection.
     *
     * @param field  The field to get
     * @param target The object containing the field
     * @return The value of the field
     * @throws ApiException If the field value cannot be retrieved using VarHandle
     */
    private static Object getFieldValueWithHandles(Field field, Object target) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(target.getClass(), MethodHandles.lookup());
            VarHandle varHandle = lookup.unreflectVarHandle(field);
            return varHandle.get(target);
        } catch (Exception ex) {
            throw new ApiException("Error getting field value via VarHandle: " + field.getName(), ex);
        }
    }

    /**
     * Checks if the value returned by a supplier is null.
     * <p>
     * This method is a convenience wrapper for null checking.
     *
     * @param <T>            The type of the value
     * @param getterValidate A supplier that provides the value to check
     * @return true if the value is null, false otherwise
     */
    public static <T> boolean verifyNull(Supplier<T> getterValidate) {
        T value = getterValidate.get();
        return value == null;
    }
}