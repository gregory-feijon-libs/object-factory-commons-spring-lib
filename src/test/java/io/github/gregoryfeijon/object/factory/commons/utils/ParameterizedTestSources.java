package io.github.gregoryfeijon.object.factory.commons.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

/**
 * Provides commonly used argument providers for parameterized tests
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParameterizedTestSources {

    /**
     * Provides various null/empty/blank string combinations
     */
    public static Stream<Arguments> invalidStringArguments() {
        return Stream.of(
                Arguments.of((Object) null, "null"),
                Arguments.of("", "empty"),
                Arguments.of("  ", "blank with spaces"),
                Arguments.of("\t", "blank with tab"),
                Arguments.of("\n", "blank with newline"),
                Arguments.of("   \t\n   ", "blank mixed whitespace")
        );
    }

    /**
     * Provides valid string test cases
     */
    public static Stream<Arguments> validStringArguments() {
        return Stream.of(
                Arguments.of("simple", "Simple string"),
                Arguments.of("with-dash", "String with dash"),
                Arguments.of("with_underscore", "String with underscore"),
                Arguments.of("with.dot", "String with dot"),
                Arguments.of("CamelCase", "CamelCase string"),
                Arguments.of("123numeric", "String starting with number"),
                Arguments.of("special!@#", "String with special chars")
        );
    }

    /**
     * Provides numeric test cases (positive, negative, zero)
     */
    public static Stream<Arguments> numericArguments() {
        return Stream.of(
                Arguments.of(0, "zero"),
                Arguments.of(1, "positive small"),
                Arguments.of(100, "positive large"),
                Arguments.of(-1, "negative small"),
                Arguments.of(-100, "negative large"),
                Arguments.of(Integer.MAX_VALUE, "max integer"),
                Arguments.of(Integer.MIN_VALUE, "min integer")
        );
    }
}