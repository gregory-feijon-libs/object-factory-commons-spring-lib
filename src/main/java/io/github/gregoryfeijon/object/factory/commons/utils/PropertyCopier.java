package io.github.gregoryfeijon.object.factory.commons.utils;

import io.github.gregoryfeijon.object.factory.commons.exception.ApiException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for copying properties between objects by matching getter/setter names.
 * <p>
 * Extracted from {@link ReflectionUtil} to keep property-copying concerns separate
 * from method/field discovery. Delegates the actual dynamic getter/setter invocation
 * to {@link DynamicAccessorUtil}.
 * <p>
 * <strong>Thread-Safety:</strong> All methods are stateless and thread-safe.
 *
 * @author gregory.feijon
 * @since 1.0
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PropertyCopier {

    /**
     * Copies properties from a source object to a target object.
     * <p>
     * This method copies values from all matching properties (by name) from the source
     * to the target. Properties are matched using getters from source and setters on target.
     * <p>
     * <strong>Example:</strong>
     * <pre>
     * User source = new User("John", 25, "john@email.com");
     * UserDTO target = new UserDTO();
     * copyProperties(source, target); // Copies name, age, email
     * </pre>
     *
     * @param <S>    The source type
     * @param <T>    The target type
     * @param source The source object to copy from (must not be null)
     * @param target The target object to copy to (must not be null)
     * @throws ApiException If source or target is null, or if copying fails
     */
    public static <S, T> void copyProperties(S source, T target) {
        copyProperties(source, target, new String[0]);
    }

    /**
     * Copies properties from a source object to a target object, ignoring specified properties.
     * <p>
     * This method copies values from matching properties (by name) from the source
     * to the target, excluding properties listed in ignoreProperties.
     * <p>
     * <strong>Example:</strong>
     * <pre>
     * User source = new User("John", 25, "john@email.com", LocalDateTime.now());
     * UserDTO target = new UserDTO();
     * copyProperties(source, target, "createdAt", "updatedAt"); // Ignores timestamps
     * </pre>
     *
     * @param <S>              The source type
     * @param <T>              The target type
     * @param source           The source object to copy from (must not be null)
     * @param target           The target object to copy to (must not be null)
     * @param ignoreProperties Property names to ignore during copy (can be empty)
     * @throws ApiException If source or target is null, or if copying fails
     */
    public static <S, T> void copyProperties(S source, T target, String... ignoreProperties) {
        ReflectionValidationUtil.validateObject(source, "Source object");
        ReflectionValidationUtil.validateObject(target, "Target object");

        List<String> ignoreList = ignoreProperties != null
                ? Arrays.asList(ignoreProperties)
                : List.of();

        Collection<Field> sourceFields = ReflectionUtil.getFieldsAsCollection(source);
        List<Method> targetSetters = ReflectionUtil.findSetMethods(target);

        for (Field sourceField : sourceFields) {
            String fieldName = sourceField.getName();

            if (ignoreList.contains(fieldName)) {
                continue;
            }

            copyFieldValue(source, target, sourceField, fieldName, targetSetters);
        }
    }

    /**
     * Copies a single field value from source to target.
     *
     * @param source        The source object
     * @param target        The target object
     * @param sourceField   The field to copy
     * @param fieldName     The name of the field
     * @param targetSetters Available setters on target
     */
    private static <S, T> void copyFieldValue(
            S source,
            T target,
            Field sourceField,
            String fieldName,
            List<Method> targetSetters) {

        String setterName = "set" + StringUtils.capitalize(fieldName);

        Optional<Method> targetSetter = targetSetters.stream()
                .filter(setter -> setter.getName().equalsIgnoreCase(setterName))
                .findFirst();

        if (targetSetter.isEmpty()) {
            return;
        }

        try {
            Object value = DynamicAccessorUtil.getValueDynamicallyThroughGetterNameFromField(sourceField, source);
            DynamicAccessorUtil.setValueDynamicallyThroughSetterName(setterName, target, value);
        } catch (ApiException e) {
            log.debug("Problem copying field: {}", fieldName, e);
        }
    }
}
