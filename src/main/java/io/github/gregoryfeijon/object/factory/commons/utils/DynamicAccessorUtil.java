package io.github.gregoryfeijon.object.factory.commons.utils;

import io.github.gregoryfeijon.object.factory.commons.exception.ApiException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Utility class for invoking getter/setter methods dynamically by name.
 * <p>
 * Extracted from {@link ReflectionUtil} to keep dynamic accessor invocation
 * (including automatic primitive/wrapper type conversion) separate from
 * method/field discovery.
 * <p>
 * <strong>Type Conversion Strategy:</strong> Setter invocation tries an ordered
 * chain of conversion strategies, mirroring the fallback-chain approach used in
 * {@link FieldUtil}: null handling, primitive parameter conversion, wrapper
 * conversion, and finally direct assignment.
 * <p>
 * <strong>Thread-Safety:</strong> All methods are stateless and thread-safe.
 *
 * @author gregory.feijon
 * @since 1.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DynamicAccessorUtil {

    /**
     * Functional interface for a single setter type-conversion strategy.
     * <p>
     * Returns {@code true} if the strategy applied and the setter was invoked,
     * {@code false} if it doesn't apply so the next strategy in the chain should be tried.
     */
    @FunctionalInterface
    private interface SetterConversionStrategy {
        boolean tryInvoke(Method setter, Object target, Object valueToSet, Class<?> paramType, Class<?> valueType)
                throws IllegalAccessException, InvocationTargetException;
    }

    /**
     * Ordered list of setter type-conversion strategies to try.
     */
    private static final List<SetterConversionStrategy> CONVERSION_STRATEGIES = List.of(
            DynamicAccessorUtil::invokeForNullValue,
            DynamicAccessorUtil::invokeForPrimitiveParam,
            DynamicAccessorUtil::invokeForWrapperConversion,
            DynamicAccessorUtil::invokeForDirectAssignment
    );

    /**
     * Gets the value of a field using its getter method.
     * <p>
     * This method dynamically constructs a getter name based on the field name:
     * <ul>
     *   <li>For boolean fields: isFieldName()</li>
     *   <li>For other fields: getFieldName()</li>
     * </ul>
     * <p>
     * The getter must be public to be invoked.
     * <p>
     * <strong>Example:</strong>
     * <pre>
     * Field nameField = User.class.getDeclaredField("name");
     * Object value = getValueDynamicallyThroughGetterNameFromField(nameField, user);
     * // Invokes user.getName()
     * </pre>
     *
     * @param <T>          The type of the object containing the field
     * @param field        The field to get the value for (must not be null)
     * @param getterObject The object containing the field (must not be null)
     * @return The value of the field
     * @throws ApiException If the getter cannot be found or invoked, or if parameters are null
     */
    public static <T> Object getValueDynamicallyThroughGetterNameFromField(java.lang.reflect.Field field, T getterObject) {
        ReflectionValidationUtil.validateField(field);
        ReflectionValidationUtil.validateObject(getterObject, "Getter object");

        String getterPrefix = field.getType() == boolean.class ? "is" : "get";
        String getterName = getterPrefix + StringUtils.capitalize(field.getName());

        return getValueDynamicallyThroughGetterName(getterName, getterObject);
    }

    /**
     * Gets a value by invoking a getter method by name.
     * <p>
     * The getter must:
     * <ul>
     *   <li>Exist in the object's class or its hierarchy</li>
     *   <li>Be public</li>
     *   <li>Take no parameters</li>
     * </ul>
     * <p>
     * <strong>Example:</strong>
     * <pre>
     * Object value = getValueDynamicallyThroughGetterName("getName", user);
     * // Invokes user.getName()
     * </pre>
     *
     * @param <T>          The type of the object containing the getter
     * @param getterName   The name of the getter method (must not be null or empty)
     * @param getterObject The object containing the getter (must not be null)
     * @return The value returned by the getter
     * @throws ApiException If the getter cannot be found, is not public, or invocation fails
     */
    public static <T> Object getValueDynamicallyThroughGetterName(String getterName, T getterObject) {
        ReflectionValidationUtil.validateMethodName(getterName, "Getter");
        ReflectionValidationUtil.validateObject(getterObject, "Getter object");

        Method getter = findGetterMethod(getterName, getterObject);

        if (!Modifier.isPublic(getter.getModifiers())) {
            throw new ApiException("Getter method is not public: " + getterName);
        }

        try {
            return getter.invoke(getterObject);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ApiException("Error invoking getter: " + getterName, e);
        }
    }

    /**
     * Sets the value of a field using its setter method.
     * <p>
     * This method dynamically constructs a setter name based on the field name:
     * setFieldName()
     * <p>
     * The setter must be public to be invoked. Handles type conversions between
     * primitives and their wrapper types automatically.
     * <p>
     * <strong>Example:</strong>
     * <pre>
     * Field ageField = User.class.getDeclaredField("age");
     * setValueDynamicallyThroughSetterNameFromField(ageField, user, 30);
     * // Invokes user.setAge(30)
     * </pre>
     *
     * @param <T>   The type of the object containing the field
     * @param <S>   The type of the value to set
     * @param field The field to set (must not be null)
     * @param dest  The object containing the field (must not be null)
     * @param value The value to set (can be null)
     * @throws ApiException If the setter cannot be found or invoked, or if parameters are invalid
     */
    public static <T, S> void setValueDynamicallyThroughSetterNameFromField(java.lang.reflect.Field field, T dest, S value) {
        ReflectionValidationUtil.validateField(field);
        ReflectionValidationUtil.validateObject(dest, "Destination object");

        String setterName = "set" + StringUtils.capitalize(field.getName());
        setValueDynamicallyThroughSetterName(setterName, dest, value);
    }

    /**
     * Sets a value by invoking a setter method by name.
     * <p>
     * The setter must:
     * <ul>
     *   <li>Exist in the object's class or its hierarchy</li>
     *   <li>Be public</li>
     *   <li>Take exactly one parameter</li>
     * </ul>
     * <p>
     * Automatically handles type conversions:
     * <ul>
     *   <li>Primitive ↔ Wrapper (int ↔ Integer)</li>
     *   <li>Null values: Sets primitive default values (0, false, etc.)</li>
     * </ul>
     * <p>
     * <strong>Example:</strong>
     * <pre>
     * setValueDynamicallyThroughSetterName("setAge", user, 30);
     * // Invokes user.setAge(30)
     * </pre>
     *
     * @param <T>        The type of the object containing the setter
     * @param <S>        The type of the value to set
     * @param setterName The name of the setter method (must not be null or empty)
     * @param target     The object containing the setter (must not be null)
     * @param valueToSet The value to set (can be null)
     * @throws ApiException If the setter cannot be found, is not public, or invocation fails
     */
    public static <T, S> void setValueDynamicallyThroughSetterName(String setterName, T target, S valueToSet) {
        ReflectionValidationUtil.validateMethodName(setterName, "Setter");
        ReflectionValidationUtil.validateObject(target, "Target object");

        Method setter = findSetterMethod(setterName, target);

        if (!Modifier.isPublic(setter.getModifiers())) {
            throw new ApiException("Setter method is not public: " + setterName);
        }

        Class<?> paramType = setter.getParameterTypes()[0];
        Class<?> valueType = valueToSet != null ? valueToSet.getClass() : null;

        try {
            invokeSetterWithTypeConversion(setter, target, valueToSet, paramType, valueType);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new ApiException("Error invoking setter: " + setterName, e);
        }
    }

    /**
     * Invokes the setter, trying each conversion strategy in order until one applies.
     *
     * @param setter     The setter method to invoke
     * @param target     The target object
     * @param valueToSet The value to set
     * @param paramType  The setter's parameter type
     * @param valueType  The value's actual type
     * @throws IllegalAccessException    If setter cannot be accessed
     * @throws InvocationTargetException If setter invocation fails
     * @throws ApiException              If no strategy applies (incompatible types)
     */
    private static void invokeSetterWithTypeConversion(
            Method setter,
            Object target,
            Object valueToSet,
            Class<?> paramType,
            Class<?> valueType) throws IllegalAccessException, InvocationTargetException {

        for (SetterConversionStrategy strategy : CONVERSION_STRATEGIES) {
            if (strategy.tryInvoke(setter, target, valueToSet, paramType, valueType)) {
                return;
            }
        }
        throwIncompatibleTypeException(paramType, valueType);
    }

    /**
     * Handles null value invocation for setter.
     *
     * @return true if invocation was handled
     */
    private static boolean invokeForNullValue(
            Method setter,
            Object target,
            Object valueToSet,
            Class<?> paramType,
            Class<?> valueType) throws IllegalAccessException, InvocationTargetException {

        if (valueToSet != null) {
            return false;
        }

        if (paramType.isPrimitive()) {
            setter.invoke(target, ReflectionTypeUtil.defaultValueFor(paramType));
        } else {
            setter.invoke(target, (Object) null);
        }
        return true;
    }

    /**
     * Handles primitive parameter type conversion.
     *
     * @return true if invocation was handled
     */
    private static boolean invokeForPrimitiveParam(
            Method setter,
            Object target,
            Object valueToSet,
            Class<?> paramType,
            Class<?> valueType) throws IllegalAccessException, InvocationTargetException {

        if (!paramType.isPrimitive()) {
            return false;
        }

        Class<?> wrapperType = ClassUtils.primitiveToWrapper(paramType);
        if (wrapperType.isAssignableFrom(valueType)) {
            setter.invoke(target, valueToSet);
            return true;
        }
        return false;
    }

    /**
     * Handles wrapper to primitive conversion.
     *
     * @return true if invocation was handled
     */
    private static boolean invokeForWrapperConversion(
            Method setter,
            Object target,
            Object valueToSet,
            Class<?> paramType,
            Class<?> valueType) throws IllegalAccessException, InvocationTargetException {

        if (valueType == null) {
            return false;
        }

        Class<?> wrapperType = ClassUtils.primitiveToWrapper(valueType);
        if (wrapperType.isAssignableFrom(paramType)) {
            setter.invoke(target, valueToSet);
            return true;
        }
        return false;
    }

    /**
     * Handles direct assignment when types are compatible.
     *
     * @return true if invocation was handled
     */
    private static boolean invokeForDirectAssignment(
            Method setter,
            Object target,
            Object valueToSet,
            Class<?> paramType,
            Class<?> valueType) throws IllegalAccessException, InvocationTargetException {

        if (valueType != null && paramType.isAssignableFrom(valueType)) {
            setter.invoke(target, valueToSet);
            return true;
        }
        return false;
    }

    /**
     * Throws exception for incompatible types.
     */
    private static void throwIncompatibleTypeException(Class<?> paramType, Class<?> valueType) {
        throw new ApiException(String.format(
                "Incompatible parameter type for setter: expected %s but got %s",
                paramType.getName(),
                valueType != null ? valueType.getName() : "null"
        ));
    }

    /**
     * Finds a getter method by name in the object's class hierarchy.
     *
     * @param <T>          The type of the object
     * @param getterName   The name of the getter
     * @param getterObject The object to search in
     * @return The getter method
     * @throws ApiException If getter is not found or no getters exist
     */
    private static <T> Method findGetterMethod(String getterName, T getterObject) {
        return findMethodInList(ReflectionUtil.findGetMethods(getterObject), getterName, "getter");
    }

    /**
     * Finds a setter method by name in the object's class hierarchy.
     *
     * @param <T>         The type of the object
     * @param setterName  The name of the setter
     * @param setterClass The object to search in
     * @return The setter method
     * @throws ApiException If setter is not found or no setters exist
     */
    private static <T> Method findSetterMethod(String setterName, T setterClass) {
        return findMethodInList(ReflectionUtil.findSetMethods(setterClass), setterName, "setter");
    }

    /**
     * Finds a method by name in a list of methods.
     *
     * @param methods    The list of methods to search
     * @param methodName The name of the method to find
     * @param methodType Description of the method type (e.g., "getter", "setter")
     * @return The found method
     * @throws ApiException If no methods exist or the method is not found
     */
    private static Method findMethodInList(List<Method> methods, String methodName, String methodType) {
        if (CollectionUtils.isEmpty(methods)) {
            throw new ApiException("There's no " + methodType + " method in specified Object!");
        }

        return methods.stream()
                .filter(method -> method.getName().equalsIgnoreCase(methodName))
                .findAny()
                .orElseThrow(() -> new ApiException(
                        "There's no " + methodType + " with specified name: " + methodName));
    }
}
