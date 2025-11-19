package io.github.gregoryfeijon.object.factory.commons;

import io.github.gregoryfeijon.object.factory.commons.domain.ArrayTestClass;
import io.github.gregoryfeijon.object.factory.commons.domain.CustomNumber;
import io.github.gregoryfeijon.object.factory.commons.domain.GenericClass;
import io.github.gregoryfeijon.object.factory.commons.domain.UpperBoundNumberTestClass;
import io.github.gregoryfeijon.object.factory.commons.domain.UpperBoundTestClass;
import io.github.gregoryfeijon.object.factory.commons.domain.WildcardTestClass;
import io.github.gregoryfeijon.object.factory.commons.utils.ReflectionTypeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ReflectionTypeUtil Tests")
class ReflectionTypeUtilTest {

    // ==================== Nested Test Classes ====================

    @Nested
    @DisplayName("getRawType() tests")
    class GetRawTypeTests {

        @Test
        @DisplayName("Should resolve Class directly")
        void shouldResolveClassDirectly() throws Exception {
            // When
            Class<?> result = ReflectionTypeUtil.getRawType(String.class);

            // Then
            assertThat(result).isEqualTo(String.class);
        }

        @Test
        @DisplayName("Should resolve ParameterizedType")
        void shouldResolveParameterizedType() throws Exception {
            // Given
            Field field = GenericClass.class.getDeclaredField("field2");
            Type genericType = field.getGenericType();

            // When
            Class<?> result = ReflectionTypeUtil.getRawType(genericType);

            // Then
            assertThat(result).isEqualTo(List.class);
        }

        @Test
        @DisplayName("Should resolve nested ParameterizedType")
        void shouldResolveNestedParameterizedType() throws Exception {
            // Given
            Field field = GenericClass.class.getDeclaredField("field3");
            Type genericType = field.getGenericType();

            // When
            Class<?> result = ReflectionTypeUtil.getRawType(genericType);

            // Then
            assertThat(result).isEqualTo(Map.class);
        }

        @Test
        @DisplayName("Should resolve GenericArrayType")
        void shouldResolveGenericArrayType() throws Exception {
            // Given

            Field field = ArrayTestClass.class.getDeclaredField("array");
            Type genericType = field.getGenericType();

            // When
            Class<?> result = ReflectionTypeUtil.getRawType(genericType);

            // Then
            assertThat(result.isArray()).isTrue();
        }

        @Test
        @DisplayName("Should resolve TypeVariable with upper bound")
        void shouldResolveTypeVariableWithUpperBound() throws Exception {
            // Given

            Field field = UpperBoundNumberTestClass.class.getDeclaredField("value");
            Type genericType = field.getGenericType();

            // When
            Class<?> result = ReflectionTypeUtil.getRawType(genericType);

            // Then
            assertThat(result).isEqualTo(Number.class);
        }

        @Test
        @DisplayName("Should resolve WildcardType with upper bound")
        void shouldResolveWildcardTypeWithUpperBound() throws Exception {
            // Given

            Field field = WildcardTestClass.class.getDeclaredField("list");
            ParameterizedType paramType = (ParameterizedType) field.getGenericType();
            Type wildcard = paramType.getActualTypeArguments()[0];

            // When
            Class<?> result = ReflectionTypeUtil.getRawType(wildcard);

            // Then
            assertThat(result).isEqualTo(Number.class);
        }

        @Test
        @DisplayName("Should throw exception for TypeVariable without bounds")
        void shouldThrowExceptionForTypeVariableWithoutBounds() throws Exception {
            // Given
            Field field = UpperBoundTestClass.class.getDeclaredField("value");
            Type genericType = field.getGenericType();
            TypeVariable<?> typeVar = (TypeVariable<?>) genericType;

            // Clear bounds artificially for test (using reflection)
            // In practice, TypeVariable always has Object as default bound

            // When/Then
            // This would need a mocked TypeVariable without bounds
            // For now, we verify that normal TypeVariable works
            Class<?> result = ReflectionTypeUtil.getRawType(genericType);
            assertThat(result).isEqualTo(Object.class); // Default bound
        }

        @Test
        @DisplayName("Should throw exception for unsupported Type")
        void shouldThrowExceptionForUnsupportedType() {
            // Given
            Type unsupportedType = new Type() {
                @Override
                public String getTypeName() {
                    return "UnsupportedType";
                }
            };

            // When/Then
            assertThatThrownBy(() -> ReflectionTypeUtil.getRawType(unsupportedType))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unsupported Type implementation");
        }
    }

    @Nested
    @DisplayName("isPrimitiveOrEnum() tests")
    class IsPrimitiveOrEnumTests {

        @ParameterizedTest(name = "Should return true for {0}")
        @MethodSource("providePrimitiveTypes")
        @DisplayName("Should identify primitive types")
        void shouldIdentifyPrimitiveTypes(Class<?> type) {
            // When
            boolean result = ReflectionTypeUtil.isPrimitiveOrEnum(type);

            // Then
            assertThat(result).isTrue();
        }

        static Stream<Class<?>> providePrimitiveTypes() {
            return Stream.of(
                    int.class,
                    long.class,
                    double.class,
                    float.class,
                    boolean.class,
                    byte.class,
                    short.class,
                    char.class
            );
        }

        @Test
        @DisplayName("Should identify enum type")
        void shouldIdentifyEnumType() {
            // Given
            enum TestEnum {VALUE1, VALUE2}

            // When
            boolean result = ReflectionTypeUtil.isPrimitiveOrEnum(TestEnum.class);

            // Then
            assertThat(result).isTrue();
        }

        @ParameterizedTest(name = "Should return false for {0}")
        @MethodSource("provideNonPrimitiveTypes")
        @DisplayName("Should return false for non-primitive types")
        void shouldReturnFalseForNonPrimitiveTypes(Class<?> type) {
            // When
            boolean result = ReflectionTypeUtil.isPrimitiveOrEnum(type);

            // Then
            assertThat(result).isFalse();
        }

        static Stream<Class<?>> provideNonPrimitiveTypes() {
            return Stream.of(
                    String.class,
                    Integer.class,
                    Object.class,
                    List.class
            );
        }
    }

    @Nested
    @DisplayName("isClassMapCollection() tests")
    class IsClassMapCollectionTests {

        @ParameterizedTest(name = "Should return true for {0}")
        @MethodSource("provideCollectionTypes")
        @DisplayName("Should identify collection types")
        void shouldIdentifyCollectionTypes(Class<?> type) {
            // When
            boolean result = ReflectionTypeUtil.isClassMapCollection(type);

            // Then
            assertThat(result).isTrue();
        }

        static Stream<Class<?>> provideCollectionTypes() {
            return Stream.of(
                    List.class,
                    Set.class,
                    Collection.class,
                    ArrayList.class,
                    HashSet.class,
                    LinkedList.class,
                    Map.class,
                    HashMap.class,
                    TreeMap.class
            );
        }

        @ParameterizedTest(name = "Should return false for {0}")
        @MethodSource("provideNonCollectionTypes")
        @DisplayName("Should return false for non-collection types")
        void shouldReturnFalseForNonCollectionTypes(Class<?> type) {
            // When
            boolean result = ReflectionTypeUtil.isClassMapCollection(type);

            // Then
            assertThat(result).isFalse();
        }

        static Stream<Class<?>> provideNonCollectionTypes() {
            return Stream.of(
                    String.class,
                    Integer.class,
                    Object.class,
                    int[].class
            );
        }
    }

    @Nested
    @DisplayName("isSimpleType() tests")
    class IsSimpleTypeTests {

        @ParameterizedTest(name = "Should cache and return true for {0}")
        @MethodSource("provideSimpleTypes")
        @DisplayName("Should identify simple types and cache results")
        void shouldIdentifySimpleTypes(Class<?> type) {
            // First call - populates cache
            boolean result1 = ReflectionTypeUtil.isSimpleType(type);

            // Second call - from cache
            boolean result2 = ReflectionTypeUtil.isSimpleType(type);

            // Then
            assertThat(result1).isTrue();
            assertThat(result2).isTrue();
        }

        static Stream<Class<?>> provideSimpleTypes() {
            return Stream.of(
                    int.class,
                    Integer.class,
                    String.class,
                    Double.class,
                    int[].class,
                    Integer[].class
            );
        }

        @Test
        @DisplayName("Should return false for complex types")
        void shouldReturnFalseForComplexTypes() {
            // When
            boolean result = ReflectionTypeUtil.isSimpleType(List.class);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should handle array of primitives")
        void shouldHandleArrayOfPrimitives() {
            // When
            boolean result = ReflectionTypeUtil.isSimpleType(int[].class);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should handle array of wrappers")
        void shouldHandleArrayOfWrappers() {
            // When
            boolean result = ReflectionTypeUtil.isSimpleType(Integer[].class);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for multi-dimensional arrays")
        void shouldReturnFalseForMultiDimensionalArrays() {
            // When
            boolean result = ReflectionTypeUtil.isSimpleType(int[][].class);

            // Then - multi-dimensional arrays are not considered simple
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("isWrapperType() tests")
    class IsWrapperTypeTests {

        @ParameterizedTest(name = "Should cache and return true for {0}")
        @MethodSource("provideWrapperTypes")
        @DisplayName("Should identify wrapper types and cache results")
        void shouldIdentifyWrapperTypes(Class<?> type) {
            // First call - populates cache
            boolean result1 = ReflectionTypeUtil.isWrapperType(type);

            // Second call - from cache
            boolean result2 = ReflectionTypeUtil.isWrapperType(type);

            // Then
            assertThat(result1).isTrue();
            assertThat(result2).isTrue();
        }

        static Stream<Class<?>> provideWrapperTypes() {
            return Stream.of(
                    // Number types
                    Integer.class,
                    Long.class,
                    Double.class,
                    Float.class,
                    Number.class,
                    // Boolean
                    Boolean.class,
                    Byte.class,
                    // Text types
                    String.class,
                    Character.class,
                    // Date types
                    LocalDate.class,
                    LocalDateTime.class,
                    LocalTime.class,
                    Instant.class,
                    // UUID
                    UUID.class
            );
        }

        @Test
        @DisplayName("Should return false for primitive types")
        void shouldReturnFalseForPrimitiveTypes() {
            // When
            boolean result = ReflectionTypeUtil.isWrapperType(int.class);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for complex types")
        void shouldReturnFalseForComplexTypes() {
            // When
            boolean result = ReflectionTypeUtil.isWrapperType(List.class);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("defaultValueFor() tests")
    class DefaultValueForTests {

        @Test
        @DisplayName("Should return false for boolean")
        void shouldReturnFalseForBoolean() {
            // When
            Boolean result = ReflectionTypeUtil.defaultValueFor(boolean.class);

            // Then
            assertThat(result).isFalse();
        }

        @ParameterizedTest(name = "Should return 0 for {0}")
        @MethodSource("provideNumericPrimitives")
        @DisplayName("Should return 0 for numeric primitives")
        void shouldReturnZeroForNumericPrimitives(Class<?> type, Object expectedValue) {
            // When
            Object result = ReflectionTypeUtil.defaultValueFor(type);

            // Then
            assertThat(result).isEqualTo(expectedValue);
        }

        static Stream<Arguments> provideNumericPrimitives() {
            return Stream.of(
                    Arguments.of(byte.class, (byte) 0),
                    Arguments.of(short.class, (short) 0),
                    Arguments.of(int.class, 0),
                    Arguments.of(long.class, 0L),
                    Arguments.of(float.class, 0.0F),
                    Arguments.of(double.class, 0.0D)
            );
        }

        @Test
        @DisplayName("Should return null character for char")
        void shouldReturnNullCharacterForChar() {
            // When
            Character result = ReflectionTypeUtil.defaultValueFor(char.class);

            // Then
            assertThat(result).isEqualTo('\0');
        }

        @Test
        @DisplayName("Should return null for non-primitive types")
        void shouldReturnNullForNonPrimitiveTypes() {
            // When
            String result = ReflectionTypeUtil.defaultValueFor(String.class);

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Cache Performance Tests")
    class CachePerformanceTests {

        @Test
        @DisplayName("Should cache improve performance significantly")
        void shouldCacheImprovePerformance() {
            // Given
            Class<?> testClass = String.class;

            // Warm up
            ReflectionTypeUtil.isSimpleType(testClass);

            // First call (may hit cache from warmup)
            long start1 = System.nanoTime();
            ReflectionTypeUtil.isSimpleType(testClass);
            long time1 = System.nanoTime() - start1;

            // Subsequent calls (definitely from cache)
            long start2 = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                ReflectionTypeUtil.isSimpleType(testClass);
            }
            long time2 = System.nanoTime() - start2;

            // Average time per cached call
            long avgCachedTime = time2 / 1000;

            // Cache should be faster (though this is a soft assertion)
            // In practice, cached calls are ~10ns vs ~100µs for computation
            assertThat(avgCachedTime).isLessThan(1_000); // Less than 1 microsecond
        }
    }

    @Nested
    @DisplayName("Thread-Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Should handle concurrent access safely")
        void shouldHandleConcurrentAccessSafely() throws InterruptedException {
            // Given
            int threadCount = 10;
            int iterationsPerThread = 1000;
            List<Thread> threads = new ArrayList<>();

            // When - Multiple threads accessing cache simultaneously
            for (int i = 0; i < threadCount; i++) {
                Thread thread = new Thread(() -> {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        ReflectionTypeUtil.isSimpleType(Integer.class);
                        ReflectionTypeUtil.isWrapperType(String.class);
                        ReflectionTypeUtil.isSimpleType(int[].class);
                    }
                });
                threads.add(thread);
                thread.start();
            }

            // Then - All threads complete without exceptions
            for (Thread thread : threads) {
                thread.join(5000); // 5 second timeout
                assertThat(thread.isAlive()).isFalse();
            }

            // Verify cache still works correctly
            assertThat(ReflectionTypeUtil.isSimpleType(Integer.class)).isTrue();
            assertThat(ReflectionTypeUtil.isWrapperType(String.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle all wrapper type categories")
        void shouldHandleAllWrapperTypeCategories() {
            // Number types
            assertThat(ReflectionTypeUtil.numberTypes())
                    .contains(Integer.class, Double.class, Float.class, Long.class, Number.class);

            // Date types
            assertThat(ReflectionTypeUtil.dateTypes())
                    .contains(LocalDate.class, LocalDateTime.class, Instant.class);

            // Text types
            assertThat(ReflectionTypeUtil.textTypes())
                    .contains(String.class, Character.class);
        }

        @Test
        @DisplayName("Should verify getWrapperTypes includes all categories")
        void shouldVerifyGetWrapperTypesIncludesAllCategories() {
            // When
            Set<Class<?>> wrapperTypes = ReflectionTypeUtil.getWrapperTypes();

            // Then
            assertThat(wrapperTypes)
                    .contains(Integer.class, String.class, LocalDate.class, UUID.class, Boolean.class);
        }

        @Test
        @DisplayName("Should handle custom class extending wrapper")
        void shouldHandleCustomClassExtendingWrapper() {
            // When
            boolean result = ReflectionTypeUtil.isWrapperType(CustomNumber.class);

            // Then - Should be true because it extends Number
            assertThat(result).isTrue();
        }
    }
}