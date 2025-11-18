package io.github.gregoryfeijon.object.factory.commons.enums;

import io.github.gregoryfeijon.object.factory.commons.domain.enums.EmptyEnum;
import io.github.gregoryfeijon.object.factory.commons.domain.enums.NullValueEnum;
import io.github.gregoryfeijon.object.factory.commons.domain.enums.StatusEnum;
import io.github.gregoryfeijon.object.factory.commons.exception.ApiException;
import io.github.gregoryfeijon.object.factory.commons.utils.enums.EnumUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EnumUtil Tests")
class EnumUtilTest {

    // ==================== Nested Test Classes ====================

    @Nested
    @DisplayName("getEnum() method tests")
    class GetEnumTests {

        @Test
        @DisplayName("Should find enum by matching code")
        void shouldFindEnumByCode() {
            // When
            Optional<StatusEnum> result = EnumUtil.getEnum(
                    StatusEnum.class,
                    StatusEnum::getCode,
                    "A"
            );

            // Then
            assertThat(result)
                    .isPresent()
                    .contains(StatusEnum.ACTIVE);
        }

        @Test
        @DisplayName("Should find enum by matching id")
        void shouldFindEnumById() {
            // When
            Optional<StatusEnum> result = EnumUtil.getEnum(
                    StatusEnum.class,
                    StatusEnum::getId,
                    2
            );

            // Then
            assertThat(result)
                    .isPresent()
                    .contains(StatusEnum.INACTIVE);
        }

        @Test
        @DisplayName("Should return empty Optional when no match found")
        void shouldReturnEmptyWhenNoMatch() {
            // When
            Optional<StatusEnum> result = EnumUtil.getEnum(
                    StatusEnum.class,
                    StatusEnum::getCode,
                    "NONEXISTENT"
            );

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty Optional for empty enum")
        void shouldReturnEmptyForEmptyEnum() {
            // When
            Optional<EmptyEnum> result = EnumUtil.getEnum(
                    EmptyEnum.class,
                    Enum::name,
                    "ANY"
            );

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should skip enum constants with null property values")
        void shouldSkipNullPropertyValues() {
            // When
            Optional<NullValueEnum> result = EnumUtil.getEnum(
                    NullValueEnum.class,
                    NullValueEnum::getValue,
                    "value"
            );

            // Then
            assertThat(result)
                    .isPresent()
                    .contains(NullValueEnum.WITH_VALUE);
        }

        @ParameterizedTest(name = "Should throw ApiException when {0} is null")
        @MethodSource("provideNullArguments")
        @DisplayName("Should throw ApiException for null arguments")
        void shouldThrowExceptionForNullArguments(
                String testCase,
                Class<StatusEnum> enumType,
                Function<StatusEnum, String> method,
                String expectedValue) {

            // When/Then
            assertThatThrownBy(() -> EnumUtil.getEnum(enumType, method, expectedValue))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Arguments cannot be null");
        }

        static Stream<Arguments> provideNullArguments() {
            return Stream.of(
                    Arguments.of(
                            "enumType",
                            null,
                            (Function<StatusEnum, String>) StatusEnum::getCode,
                            "A"
                    ),
                    Arguments.of(
                            "method",
                            StatusEnum.class,
                            null,
                            "A"
                    ),
                    Arguments.of(
                            "expectedValue",
                            StatusEnum.class,
                            (Function<StatusEnum, String>) StatusEnum::getCode,
                            null
                    )
            );
        }

        @Test
        @DisplayName("Should find first matching enum when multiple matches exist")
        void shouldFindFirstMatchWhenMultipleMatches() {
            // Given - using name() which all enums have
            // When
            Optional<StatusEnum> result = EnumUtil.getEnum(
                    StatusEnum.class,
                    e -> e.getClass().getSimpleName(),
                    "StatusEnum"
            );

            // Then - should return ACTIVE (first constant)
            assertThat(result)
                    .isPresent()
                    .contains(StatusEnum.ACTIVE);
        }
    }

    @Nested
    @DisplayName("getEnumOrNull() method tests")
    class GetEnumOrNullTests {

        @Test
        @DisplayName("Should find enum by matching code")
        void shouldFindEnumByCode() {
            // When
            StatusEnum result = EnumUtil.getEnumOrNull(
                    StatusEnum.class,
                    StatusEnum::getCode,
                    "P"
            );

            // Then
            assertThat(result).isEqualTo(StatusEnum.PENDING);
        }

        @Test
        @DisplayName("Should return null when no match found")
        void shouldReturnNullWhenNoMatch() {
            // When
            StatusEnum result = EnumUtil.getEnumOrNull(
                    StatusEnum.class,
                    StatusEnum::getCode,
                    "NONEXISTENT"
            );

            // Then
            assertThat(result).isNull();
        }

        @ParameterizedTest(name = "Should return null when {0} is null")
        @MethodSource("provideNullArguments")
        @DisplayName("Should return null for null arguments")
        void shouldReturnNullForNullArguments(
                String testCase,
                Class<StatusEnum> enumType,
                Function<StatusEnum, String> method,
                String expectedValue) {

            // When
            StatusEnum result = EnumUtil.getEnumOrNull(enumType, method, expectedValue);

            // Then
            assertThat(result).isNull();
        }

        static Stream<Arguments> provideNullArguments() {
            return Stream.of(
                    Arguments.of(
                            "enumType",
                            null,
                            (Function<StatusEnum, String>) StatusEnum::getCode,
                            "A"
                    ),
                    Arguments.of(
                            "method",
                            StatusEnum.class,
                            null,
                            "A"
                    ),
                    Arguments.of(
                            "expectedValue",
                            StatusEnum.class,
                            (Function<StatusEnum, String>) StatusEnum::getCode,
                            null
                    )
            );
        }

        @Test
        @DisplayName("Should handle enum with null property values")
        void shouldHandleNullPropertyValues() {
            // When
            NullValueEnum result = EnumUtil.getEnumOrNull(
                    NullValueEnum.class,
                    NullValueEnum::getValue,
                    "value"
            );

            // Then
            assertThat(result).isEqualTo(NullValueEnum.WITH_VALUE);
        }

        @Test
        @DisplayName("Should return null for empty enum")
        void shouldReturnNullForEmptyEnum() {
            // When
            EmptyEnum result = EnumUtil.getEnumOrNull(
                    EmptyEnum.class,
                    e -> e.name(),
                    "ANY"
            );

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should work with different property types")
        void shouldWorkWithDifferentPropertyTypes() {
            // Integer
            assertThat(EnumUtil.getEnum(StatusEnum.class, StatusEnum::getId, 1))
                    .isPresent()
                    .contains(StatusEnum.ACTIVE);

            // String
            assertThat(EnumUtil.getEnum(StatusEnum.class, StatusEnum::getCode, "I"))
                    .isPresent()
                    .contains(StatusEnum.INACTIVE);

            // Enum name
            assertThat(EnumUtil.getEnum(StatusEnum.class, Enum::name, "PENDING"))
                    .isPresent()
                    .contains(StatusEnum.PENDING);
        }

        @Test
        @DisplayName("Should handle complex extraction functions")
        void shouldHandleComplexExtractionFunctions() {
            // Given - using a complex function
            Function<StatusEnum, String> complexFunction = e ->
                    e.getCode() + "_" + e.getId();

            // When
            Optional<StatusEnum> result = EnumUtil.getEnum(
                    StatusEnum.class,
                    complexFunction,
                    "A_1"
            );

            // Then
            assertThat(result)
                    .isPresent()
                    .contains(StatusEnum.ACTIVE);
        }

        @Test
        @DisplayName("Both methods should behave consistently for valid inputs")
        void shouldBehaveConsistentlyForValidInputs() {
            // When
            Optional<StatusEnum> optionalResult = EnumUtil.getEnum(
                    StatusEnum.class,
                    StatusEnum::getCode,
                    "A"
            );
            StatusEnum nullableResult = EnumUtil.getEnumOrNull(
                    StatusEnum.class,
                    StatusEnum::getCode,
                    "A"
            );

            // Then
            assertThat(optionalResult).isPresent();
            assertThat(nullableResult).isEqualTo(optionalResult.get());
        }

        @Test
        @DisplayName("Both methods should behave consistently when no match found")
        void shouldBehaveConsistentlyWhenNoMatch() {
            // When
            Optional<StatusEnum> optionalResult = EnumUtil.getEnum(
                    StatusEnum.class,
                    StatusEnum::getCode,
                    "INVALID"
            );
            StatusEnum nullableResult = EnumUtil.getEnumOrNull(
                    StatusEnum.class,
                    StatusEnum::getCode,
                    "INVALID"
            );

            // Then
            assertThat(optionalResult).isEmpty();
            assertThat(nullableResult).isNull();
        }
    }
}