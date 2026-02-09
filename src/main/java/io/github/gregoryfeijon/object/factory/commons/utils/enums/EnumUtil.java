package io.github.gregoryfeijon.object.factory.commons.utils.enums;

import io.github.gregoryfeijon.object.factory.commons.exception.ApiException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Locale;
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
     * Finds the first enum constant that matches the expected value.
     *
     * @param <T>           The enum type
     * @param <R>           The property type
     * @param enumType      The enum class
     * @param method        The property extractor function
     * @param expectedValue The value to match
     * @return Optional containing the matching enum, or empty if not found
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
     * Determines if the enum constant's property value matches the expected value.
     *
     * @param <T>           The enum type
     * @param <R>           The property type
     * @param enumConstant  The enum constant to check
     * @param method        The property extractor function
     * @param expectedValue The value to match against
     * @return true if the property value equals the expected value
     */
    private static <T extends Enum<T>, R> boolean isMatchingEnum(
            final T enumConstant,
            final Function<T, R> method,
            final R expectedValue) {

        R value = method.apply(enumConstant);
        return value != null && value.equals(expectedValue);
    }

    /**
     * Finds an enum constant by its name.
     * <p>
     * This method provides a safer alternative to {@link Enum#valueOf(Class, String)}
     * that returns an Optional instead of throwing an exception when the name is not found.
     * <p>
     * <strong>Example:</strong>
     * <pre>
     * Optional&lt;Status&gt; status = EnumUtil.getEnumByName(Status.class, "ACTIVE");
     * </pre>
     *
     * @param <T>       The enum type
     * @param enumType  The class object of the enum type (must not be null)
     * @param name      The name of the enum constant to find (must not be null)
     * @return An Optional containing the matching enum constant, or empty if not found
     * @throws ApiException If enumType or name is null
     */
    public static <T extends Enum<T>> Optional<T> getEnumByName(
            final Class<T> enumType,
            final String name) {

        return getEnumByName(enumType, name, false);
    }

    /**
     * Finds an enum constant by its name with optional case-insensitive matching.
     * <p>
     * <strong>Example:</strong>
     * <pre>
     * // Case-sensitive (default)
     * Optional&lt;Status&gt; status1 = EnumUtil.getEnumByName(Status.class, "ACTIVE", false);
     *
     * // Case-insensitive
     * Optional&lt;Status&gt; status2 = EnumUtil.getEnumByName(Status.class, "active", true);
     * </pre>
     *
     * @param <T>        The enum type
     * @param enumType   The class object of the enum type (must not be null)
     * @param name       The name of the enum constant to find (must not be null)
     * @param ignoreCase Whether to ignore case when matching names
     * @return An Optional containing the matching enum constant, or empty if not found
     * @throws ApiException If enumType or name is null
     */
    public static <T extends Enum<T>> Optional<T> getEnumByName(
            final Class<T> enumType,
            final String name,
            final boolean ignoreCase) {

        validateNameArguments(enumType, name);

        return Stream.of(enumType.getEnumConstants())
                .filter(enumConstant -> matchesName(enumConstant, name, ignoreCase))
                .findFirst();
    }

    /**
     * Finds an enum constant by its name, returning null if not found.
     * <p>
     * This method is similar to {@link #getEnumByName(Class, String)} but returns null
     * instead of an Optional when no matching enum constant is found.
     *
     * @param <T>       The enum type
     * @param enumType  The class object of the enum type
     * @param name      The name of the enum constant to find
     * @return The matching enum constant, or null if not found or if any argument is null
     */
    public static <T extends Enum<T>> T getEnumByNameOrNull(
            final Class<T> enumType,
            final String name) {

        return getEnumByNameOrNull(enumType, name, false);
    }

    /**
     * Finds an enum constant by its name with optional case-insensitive matching,
     * returning null if not found.
     *
     * @param <T>        The enum type
     * @param enumType   The class object of the enum type
     * @param name       The name of the enum constant to find
     * @param ignoreCase Whether to ignore case when matching names
     * @return The matching enum constant, or null if not found or if any argument is null
     */
    public static <T extends Enum<T>> T getEnumByNameOrNull(
            final Class<T> enumType,
            final String name,
            final boolean ignoreCase) {

        if (enumType == null || name == null) {
            return null;
        }

        return getEnumByName(enumType, name, ignoreCase).orElse(null);
    }

    /**
     * Determines if the enum constant's name matches the given name.
     *
     * @param <T>          The enum type
     * @param enumConstant The enum constant to check
     * @param name         The name to match against
     * @param ignoreCase   Whether to perform case-insensitive comparison
     * @return true if the names match according to the comparison mode
     */
    private static <T extends Enum<T>> boolean matchesName(
            final T enumConstant,
            final String name,
            final boolean ignoreCase) {

        if (ignoreCase) {
            return enumConstant.name().toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT));
        }
        return enumConstant.name().equals(name);
    }

    /**
     * Validates arguments for name-based enum lookup.
     *
     * @throws ApiException if enumType or name is null
     */
    private static <T extends Enum<T>> void validateNameArguments(
            final Class<T> enumType,
            final String name) {

        if (enumType == null || name == null) {
            throw new ApiException("Arguments cannot be null: enumType and name are required");
        }
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
     * Verifies if any of the provided arguments is null.
     *
     * @param <T>           The enum type
     * @param <R>           The property type
     * @param enumType      The enum class to check
     * @param method        The property extractor function to check
     * @param expectedValue The expected value to check
     * @return true if any argument is null
     */
    private static <T extends Enum<T>, R> boolean hasNullValues(
            final Class<T> enumType,
            final Function<T, R> method,
            final R expectedValue) {

        return enumType == null || method == null || expectedValue == null;
    }
}