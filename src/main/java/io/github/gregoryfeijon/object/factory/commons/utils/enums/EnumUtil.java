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
    public static <T extends Enum<T>, R> Optional<T> getEnum(final Class<T> enumType, final Function<T, R> method, R expectedValue) {
        boolean hasNullValues = verifyNullValues(enumType, method, expectedValue);
        if (hasNullValues) {
            throw new ApiException("Please, define values for all arguments in getEnum method.");
        }
        return Stream.of(enumType.getEnumConstants()).filter(e -> {
            Object value = method.apply(e);
            return value != null && value.equals(expectedValue);
        }).findAny();
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
    public static <T extends Enum<T>, R> T getEnumOrNull(final Class<T> enumType, final Function<T, R> method, R expectedValue) {
        boolean hasNullValues = verifyNullValues(enumType, method, expectedValue);
        if (hasNullValues) {
            return null;
        }
        Optional<T> opEnum = getEnum(enumType, method, expectedValue);
        return opEnum.orElse(null);
    }

    /**
     * Verifies if any of the arguments is null.
     *
     * @param <T>           The enum type
     * @param <R>           The property type
     * @param enumType      The class object of the enum type
     * @param method        A function that extracts the property from an enum constant
     * @param expectedValue The expected property value to match
     * @return true if any argument is null, false otherwise
     */
    private static <T extends Enum<T>, R> boolean verifyNullValues(Class<T> enumType, Function<T, R> method, R expectedValue) {
        return (enumType == null || method == null || expectedValue == null);
    }
}