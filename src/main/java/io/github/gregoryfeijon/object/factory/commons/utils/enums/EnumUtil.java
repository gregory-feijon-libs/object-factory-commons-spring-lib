package io.github.gregoryfeijon.object.factory.commons.utils.enums;

import io.github.gregoryfeijon.object.factory.commons.exception.ApiException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Utility class for working with enums.
 * <p>
 * This class provides methods to find enum constants based on their property values.
 *
 * @author gregory.feijon
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnumUtil {

    private static final String NULL_ARGUMENTS_ERROR = "Arguments cannot be null: enumType, method, and expectedValue" +
            " are required";

    /**
     * Finds an enum constant by matching a property value.
     * <p>
     * This method searches through all constants of the given enum type and returns
     * the first one whose property (accessed via the provided method) equals the expected value.
     *
     * @param <T>           The enum type
     * @param <R>           The property type
     * @param enumType      The class object of the enum type
     * @param method        A function that extracts the property from an enum constant
     * @param expectedValue The expected property value to match
     * @return An Optional containing the matching enum constant, or empty if none found
     * @throws ApiException If any of the arguments is null
     */
    public static <T extends Enum<T>, R> Optional<T> getEnum(
            final Class<T> enumType,
            final Function<T, R> method,
            final R expectedValue) {

        validateArguments(enumType, method, expectedValue);

        return findMatchingEnum(enumType, method, expectedValue);
    }

    /**
     * Finds an enum constant by matching a property value, returning null if not found.
     * <p>
     * This method is similar to {@link #getEnum} but returns null instead of an Optional
     * when no matching enum constant is found.
     *
     * @param <T>           The enum type
     * @param <R>           The property type
     * @param enumType      The class object of the enum type
     * @param method        A function that extracts the property from an enum constant
     * @param expectedValue The expected property value to match
     * @return The matching enum constant, or null if none found or if any argument is null
     */
    public static <T extends Enum<T>, R> T getEnumOrNull(
            final Class<T> enumType,
            final Function<T, R> method,
            final R expectedValue) {

        if (hasNullValues(enumType, method, expectedValue)) {
            return null;
        }

        return findMatchingEnum(enumType, method, expectedValue).orElse(null);
    }

    /**
     * Core logic to find matching enum constant.
     * Extracts the common logic to avoid duplication.
     */
    private static <T extends Enum<T>, R> Optional<T> findMatchingEnum(
            final Class<T> enumType,
            final Function<T, R> method,
            final R expectedValue) {

        return Stream.of(enumType.getEnumConstants())
                .filter(enumConstant -> isMatchingEnum(enumConstant, method, expectedValue))
                .findFirst();
    }

    /**
     * Checks if an enum constant matches the expected value.
     */
    private static <T extends Enum<T>, R> boolean isMatchingEnum(
            final T enumConstant,
            final Function<T, R> method,
            final R expectedValue) {

        R value = method.apply(enumConstant);
        return value != null && value.equals(expectedValue);
    }

    /**
     * Validates that none of the required arguments is null.
     *
     * @throws ApiException if any argument is null
     */
    private static <T extends Enum<T>, R> void validateArguments(
            final Class<T> enumType,
            final Function<T, R> method,
            final R expectedValue) {

        if (hasNullValues(enumType, method, expectedValue)) {
            throw new ApiException(NULL_ARGUMENTS_ERROR);
        }
    }

    /**
     * Checks if any of the arguments is null.
     */
    private static <T extends Enum<T>, R> boolean hasNullValues(
            final Class<T> enumType,
            final Function<T, R> method,
            final R expectedValue) {

        return enumType == null || method == null || expectedValue == null;
    }
}