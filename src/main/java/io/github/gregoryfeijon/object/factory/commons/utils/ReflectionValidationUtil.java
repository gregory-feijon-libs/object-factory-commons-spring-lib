package io.github.gregoryfeijon.object.factory.commons.utils;

import io.github.gregoryfeijon.object.factory.commons.exception.ApiException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.function.Function;

/**
 * Shared validation helpers used internally by the reflection utility classes
 * in this package.
 * <p>
 * Package-private by design: these checks are an implementation detail of
 * {@link ReflectionUtil}, {@link ReflectionComparator}, {@link DynamicAccessorUtil}
 * and {@link PropertyCopier}, not part of the library's public API.
 *
 * @author gregory.feijon
 * @since 1.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ReflectionValidationUtil {

    static final String NULL_ENTITY_ERROR = "Entities to compare cannot be null";
    static final String DIFFERENT_TYPES_ERROR = "Entities must be of the same type";

    /**
     * Validates that an object is not null.
     *
     * @param object      The object to validate
     * @param description Description for the error message
     * @throws ApiException if object is null
     */
    static void validateObject(Object object, String description) {
        if (object == null) {
            throw new ApiException(description + " cannot be null");
        }
    }

    /**
     * Validates that a field is not null.
     *
     * @param field The field to validate
     * @throws ApiException if field is null
     */
    static void validateField(Field field) {
        if (field == null) {
            throw new ApiException("Field cannot be null");
        }
    }

    /**
     * Validates that a method name is not null or empty.
     *
     * @param methodName  The method name to validate
     * @param description Description for the error message (e.g., "Getter", "Setter")
     * @throws ApiException if method name is null or empty
     */
    static void validateMethodName(String methodName, String description) {
        if (methodName == null || methodName.trim().isEmpty()) {
            throw new ApiException(description + " method name cannot be null or empty");
        }
    }

    /**
     * Validates that a getter function is not null.
     *
     * @param getter The getter function to validate
     * @throws ApiException if getter is null
     */
    static <T, R> void validateGetter(Function<T, R> getter) {
        if (getter == null) {
            throw new ApiException("Getter function cannot be null");
        }
    }

    /**
     * Validates parameters for object comparison.
     * <p>
     * Checks that:
     * <ul>
     *   <li>Both entities are not null</li>
     *   <li>Both entities are of the same type</li>
     * </ul>
     *
     * @param entity1 First entity to validate
     * @param entity2 Second entity to validate
     * @param <T>     The type of entities
     * @throws ApiException if entities are null or of different types
     */
    static <T> void validateComparisonParameters(T entity1, T entity2) {
        if (entity1 == null || entity2 == null) {
            throw new ApiException(NULL_ENTITY_ERROR);
        }
        if (!entity1.getClass().equals(entity2.getClass())) {
            throw new ApiException(DIFFERENT_TYPES_ERROR);
        }
    }
}
