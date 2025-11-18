package io.github.gregoryfeijon.object.factory.commons.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

/**
 * Custom assertions for testing utility classes
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CustomAssertions {

    /**
     * Asserts that a class has a private constructor (utility class pattern)
     */
    public static void assertUtilityClass(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();

            assertThat(Modifier.isPrivate(constructor.getModifiers()))
                    .as("Constructor should be private for utility class")
                    .isTrue();

            constructor.setAccessible(true);

            assertThatThrownBy(constructor::newInstance)
                    .as("Constructor should throw exception when invoked")
                    .hasCauseInstanceOf(UnsupportedOperationException.class);

        } catch (NoSuchMethodException e) {
            fail("Utility class should have a no-args constructor");
        }
    }

    /**
     * Asserts that a class is final (cannot be extended)
     */
    public static void assertFinalClass(Class<?> clazz) {
        assertThat(Modifier.isFinal(clazz.getModifiers()))
                .as("Class should be final")
                .isTrue();
    }

    /**
     * Asserts that an Optional contains a value and matches a condition
     */
    public static <T> void assertOptionalContains(
            Optional<T> optional,
            java.util.function.Predicate<T> condition,
            String description) {

        assertThat(optional)
                .as("Optional should be present")
                .isPresent();

        assertThat(optional.get())
                .as(description)
                .matches(condition);
    }
}