package io.github.gregoryfeijon.object.factory.commons;

import io.github.gregoryfeijon.object.factory.commons.domain.ComparisonObject;
import io.github.gregoryfeijon.object.factory.commons.domain.MismatchedGetterObject;
import io.github.gregoryfeijon.object.factory.commons.domain.SimpleObject;
import io.github.gregoryfeijon.object.factory.commons.exception.ApiException;
import io.github.gregoryfeijon.object.factory.commons.utils.ReflectionComparator;
import io.github.gregoryfeijon.object.factory.commons.utils.ReflectionUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ReflectionComparator Tests")
class ReflectionComparatorTest {

    @Nested
    @DisplayName("compareObjectsValues() tests")
    class CompareObjectsValuesTests {

        @Test
        @DisplayName("Should return true for objects with equal values")
        void shouldReturnTrueForEqualObjects() throws Exception {
            // Given
            SimpleObject obj1 = new SimpleObject();
            SimpleObject obj2 = new SimpleObject();

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for objects with different values")
        void shouldReturnFalseForDifferentObjects() throws Exception {
            // Given
            SimpleObject obj1 = new SimpleObject();
            SimpleObject obj2 = new SimpleObject();
            obj2.setName("different");

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should treat null and empty string as equal")
        void shouldTreatNullAndEmptyStringAsEqual() throws Exception {
            // Given
            ComparisonObject obj1 = new ComparisonObject(null, 1, 1.0, List.of());
            ComparisonObject obj2 = new ComparisonObject("", 1, 1.0, List.of());

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should treat null and zero as equal for numbers")
        void shouldTreatNullAndZeroAsEqual() throws Exception {
            // Given
            ComparisonObject obj1 = new ComparisonObject("test", null, null, List.of());
            ComparisonObject obj2 = new ComparisonObject("test", 0, 0.0, List.of());

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should treat null and empty collection as equal")
        void shouldTreatNullAndEmptyCollectionAsEqual() throws Exception {
            // Given
            ComparisonObject obj1 = new ComparisonObject("test", 1, 1.0, null);
            ComparisonObject obj2 = new ComparisonObject("test", 1, 1.0, List.of());

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should compare with field filters - remove mode")
        void shouldCompareWithFieldFiltersRemoveMode() throws Exception {
            // Given
            SimpleObject obj1 = new SimpleObject();
            SimpleObject obj2 = new SimpleObject();
            obj2.setName("different");
            String[] filterNames = {"name"}; // Exclude name from comparison

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2, filterNames, true);

            // Then - Should be true because name is excluded
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should compare with field filters - include mode")
        void shouldCompareWithFieldFiltersIncludeMode() throws Exception {
            // Given
            SimpleObject obj1 = new SimpleObject();
            SimpleObject obj2 = new SimpleObject();
            obj2.setAge(99);
            String[] filterNames = {"name"}; // Only compare name

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2, filterNames, false);

            // Then - Should be true because only comparing name (which is equal)
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should handle null filter names")
        void shouldHandleNullFilterNames() throws Exception {
            // Given
            SimpleObject obj1 = new SimpleObject();
            SimpleObject obj2 = new SimpleObject();

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2, null);

            // Then - Should compare all fields
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should handle complex object comparison scenario")
        void shouldHandleComplexComparison() throws Exception {
            // Given
            ComparisonObject obj1 = new ComparisonObject("test", 42, 3.14, List.of("a", "b"));
            ComparisonObject obj2 = new ComparisonObject("test", 42, 3.14, List.of("a", "b"));

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2);

            // Then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("filterList() tests")
    class FilterListTests {

        @Test
        @DisplayName("Should filter list by removing specified methods")
        void shouldFilterListByRemoving() {
            // Given
            SimpleObject obj = new SimpleObject();
            List<Method> methods = new ArrayList<>(ReflectionUtil.findGetMethods(obj));
            String[] filterNames = {"name", "age"};

            // When
            List<Method> filtered = ReflectionComparator.filterList(methods, filterNames, true);

            // Then
            assertThat(filtered)
                    .extracting(Method::getName)
                    .doesNotContain("getName", "getAge")
                    .contains("isActive");
        }

        @Test
        @DisplayName("Should filter list by including only specified methods")
        void shouldFilterListByIncluding() {
            // Given
            SimpleObject obj = new SimpleObject();
            List<Method> methods = new ArrayList<>(ReflectionUtil.findGetMethods(obj));
            String[] filterNames = {"name"};

            // When
            List<Method> filtered = ReflectionComparator.filterList(methods, filterNames, false);

            // Then
            assertThat(filtered)
                    .extracting(Method::getName)
                    .contains("getName")
                    .doesNotContain("getAge", "isActive");
        }

        @Test
        @DisplayName("Should handle boolean getter with 'is' prefix in filter")
        void shouldHandleBooleanGetterInFilter() {
            // Given
            SimpleObject obj = new SimpleObject();
            List<Method> methods = new ArrayList<>(ReflectionUtil.findGetMethods(obj));
            String[] filterNames = {"active"}; // Will match isActive()

            // When
            List<Method> filtered = ReflectionComparator.filterList(methods, filterNames, false);

            // Then
            assertThat(filtered)
                    .extracting(Method::getName)
                    .contains("isActive");
        }

        @Test
        @DisplayName("Should return original list when no matches found")
        void shouldReturnOriginalListWhenNoMatches() {
            // Given
            SimpleObject obj = new SimpleObject();
            List<Method> methods = new ArrayList<>(ReflectionUtil.findGetMethods(obj));
            int originalSize = methods.size();
            String[] filterNames = {"nonExistentField"};

            // When
            List<Method> filtered = ReflectionComparator.filterList(methods, filterNames, true);

            // Then - Nothing removed
            assertThat(filtered).hasSize(originalSize);
        }
    }

    @Nested
    @DisplayName("filterList() setter matching tests")
    class FilterListSetterTests {

        @Test
        @DisplayName("Should filter setter methods by field name")
        void shouldFilterSetterMethodsByFieldName() {
            // Given
            SimpleObject obj = new SimpleObject();
            List<Method> methods = new ArrayList<>(ReflectionUtil.findSetMethods(obj));
            String[] filterNames = {"name"};

            // When - include mode, matching setName
            List<Method> filtered = ReflectionComparator.filterList(methods, filterNames, false);

            // Then
            assertThat(filtered)
                    .extracting(Method::getName)
                    .contains("setName");
        }

        @Test
        @DisplayName("Should remove setter methods by field name")
        void shouldRemoveSetterMethodsByFieldName() {
            // Given
            SimpleObject obj = new SimpleObject();
            List<Method> methods = new ArrayList<>(ReflectionUtil.findSetMethods(obj));
            String[] filterNames = {"name"};

            // When - remove mode
            List<Method> filtered = ReflectionComparator.filterList(methods, filterNames, true);

            // Then
            assertThat(filtered)
                    .extracting(Method::getName)
                    .doesNotContain("setName")
                    .contains("setAge", "setActive");
        }
    }

    @Nested
    @DisplayName("compareObjectsValues() validation and edge case tests")
    class CompareObjectsValuesValidationTests {

        @Test
        @DisplayName("Should throw when first entity is null")
        void shouldThrowWhenFirstEntityIsNull() {
            SimpleObject obj = new SimpleObject();
            assertThatThrownBy(() -> ReflectionComparator.compareObjectsValues(null, obj))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Entities to compare cannot be null");
        }

        @Test
        @DisplayName("Should throw when second entity is null")
        void shouldThrowWhenSecondEntityIsNull() {
            SimpleObject obj = new SimpleObject();
            assertThatThrownBy(() -> ReflectionComparator.compareObjectsValues(obj, null))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Entities to compare cannot be null");
        }

        @Test
        @DisplayName("Should throw when entities are of different types")
        void shouldThrowWhenEntitiesAreDifferentTypes() {
            assertThatThrownBy(() -> ReflectionComparator.compareObjectsValues("string", 42))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Entities must be of the same type");
        }

        @Test
        @DisplayName("Should treat empty string and null as equal (reverse order)")
        void shouldTreatEmptyStringAndNullAsEqualReverse() throws Exception {
            // Given - reversed from existing test: obj1 has value, obj2 has null
            ComparisonObject obj1 = new ComparisonObject("", 1, 1.0, List.of());
            ComparisonObject obj2 = new ComparisonObject(null, 1, 1.0, List.of());

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should treat zero and null as equal for numbers (reverse order)")
        void shouldTreatZeroAndNullAsEqualReverse() throws Exception {
            // Given - reversed: obj1 has 0, obj2 has null
            ComparisonObject obj1 = new ComparisonObject("test", 0, 0.0, List.of());
            ComparisonObject obj2 = new ComparisonObject("test", null, null, List.of());

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should treat empty collection and null as equal (reverse order)")
        void shouldTreatEmptyCollectionAndNullAsEqualReverse() throws Exception {
            // Given - reversed: obj1 has empty list, obj2 has null
            ComparisonObject obj1 = new ComparisonObject("test", 1, 1.0, List.of());
            ComparisonObject obj2 = new ComparisonObject("test", 1, 1.0, null);

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for different non-null strings")
        void shouldReturnFalseForDifferentStrings() throws Exception {
            // Given
            ComparisonObject obj1 = new ComparisonObject("hello", 1, 1.0, List.of());
            ComparisonObject obj2 = new ComparisonObject("world", 1, 1.0, List.of());

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for different non-null numbers")
        void shouldReturnFalseForDifferentNumbers() throws Exception {
            // Given
            ComparisonObject obj1 = new ComparisonObject("test", 1, 1.0, List.of());
            ComparisonObject obj2 = new ComparisonObject("test", 2, 2.0, List.of());

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for different collections")
        void shouldReturnFalseForDifferentCollections() throws Exception {
            // Given
            ComparisonObject obj1 = new ComparisonObject("test", 1, 1.0, List.of("a"));
            ComparisonObject obj2 = new ComparisonObject("test", 1, 1.0, List.of("b"));

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should compare with filter names using 3-param overload")
        void shouldCompareWithFilterNames3Param() throws Exception {
            // Given
            SimpleObject obj1 = new SimpleObject();
            SimpleObject obj2 = new SimpleObject();
            obj2.setName("different");

            // When - exclude name
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2, new String[]{"name"});

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should validate entities in 4-param compare method")
        void shouldValidateEntitiesIn4ParamCompare() {
            SimpleObject obj = new SimpleObject();
            assertThatThrownBy(() ->
                    ReflectionComparator.compareObjectsValues(null, obj, new String[]{"name"}, true))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Entities to compare cannot be null");
        }
    }

    @Nested
    @DisplayName("areValuesEquivalent edge case tests")
    class AreValuesEquivalentEdgeCaseTests {

        @Test
        @DisplayName("Should return false for non-equivalent values of unsupported type")
        void shouldReturnFalseForUnsupportedType() throws Exception {
            // Given - MismatchedGetterObject has long (not Integer/Double/String/Collection)
            // When one is null and other is non-zero, it won't match any equivalence rule
            MismatchedGetterObject obj1 = new MismatchedGetterObject("test", 5L);
            MismatchedGetterObject obj2 = new MismatchedGetterObject("test", 10L);

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2);

            // Then - different long values are not equal
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should handle non-null string vs null string comparison correctly")
        void shouldHandleNonNullVsNullString() throws Exception {
            // Given - non-empty string vs null (not equal, not empty)
            ComparisonObject obj1 = new ComparisonObject(null, 1, 1.0, List.of());
            ComparisonObject obj2 = new ComparisonObject("non-empty", 1, 1.0, List.of());

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2);

            // Then - null != "non-empty"
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should handle non-zero number vs null number comparison")
        void shouldHandleNonZeroVsNullNumber() throws Exception {
            // Given - non-zero number vs null
            ComparisonObject obj1 = new ComparisonObject("test", null, null, List.of());
            ComparisonObject obj2 = new ComparisonObject("test", 5, 5.0, List.of());

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2);

            // Then - null != 5
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should handle non-empty collection vs null collection comparison")
        void shouldHandleNonEmptyVsNullCollection() throws Exception {
            // Given - non-empty collection vs null
            ComparisonObject obj1 = new ComparisonObject("test", 1, 1.0, null);
            ComparisonObject obj2 = new ComparisonObject("test", 1, 1.0, List.of("item"));

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2);

            // Then - null != ["item"]
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("compareObjectsValues() with different object structures tests")
    class CompareObjectsDifferentStructureTests {

        @Test
        @DisplayName("Should handle both values null in comparison")
        void shouldHandleBothValuesNull() throws Exception {
            // Given - two objects where a getter returns null on both
            ComparisonObject obj1 = new ComparisonObject(null, null, null, null);
            ComparisonObject obj2 = new ComparisonObject(null, null, null, null);

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2);

            // Then - nullSafeEquals(null, null) = true
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when non-empty string vs null")
        void shouldReturnFalseNonEmptyStringVsNull() throws Exception {
            // Given - covers the case where value1 is non-empty string and value2 is null
            ComparisonObject obj1 = new ComparisonObject("hello", 1, 1.0, List.of());
            ComparisonObject obj2 = new ComparisonObject(null, 1, 1.0, List.of());

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should handle non-zero number vs null (reverse)")
        void shouldHandleNonZeroNumberVsNullReverse() throws Exception {
            // Given - covers obj1 has non-zero number, obj2 has null
            ComparisonObject obj1 = new ComparisonObject("test", 10, 10.0, List.of());
            ComparisonObject obj2 = new ComparisonObject("test", null, null, List.of());

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should handle non-empty collection vs null (reverse)")
        void shouldHandleNonEmptyCollectionVsNullReverse() throws Exception {
            // Given - obj1 has non-empty collection, obj2 has null
            ComparisonObject obj1 = new ComparisonObject("test", 1, 1.0, List.of("a"));
            ComparisonObject obj2 = new ComparisonObject("test", 1, 1.0, null);

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("String equivalence edge cases")
    class StringEquivalenceTests {

        @Test
        @DisplayName("Should consider empty string vs null as equal from both sides")
        void shouldConsiderEmptyStringAndNullEqual() throws Exception {
            // Given
            ComparisonObject obj1 = new ComparisonObject("", null, null, null);
            ComparisonObject obj2 = new ComparisonObject(null, null, null, null);

            // When
            boolean result = ReflectionComparator.compareObjectsValues(obj1, obj2);

            // Then
            assertThat(result).isTrue();
        }
    }
}
