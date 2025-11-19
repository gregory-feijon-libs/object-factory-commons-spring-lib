package io.github.gregoryfeijon.object.factory.commons;

import io.github.gregoryfeijon.object.factory.commons.domain.Child;
import io.github.gregoryfeijon.object.factory.commons.domain.ComparisonObject;
import io.github.gregoryfeijon.object.factory.commons.domain.NoGetterSetterObject;
import io.github.gregoryfeijon.object.factory.commons.domain.SimpleObject;
import io.github.gregoryfeijon.object.factory.commons.exception.ApiException;
import io.github.gregoryfeijon.object.factory.commons.utils.ReflectionUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ReflectionUtil Tests")
class ReflectionUtilTest {

    // ==================== Nested Test Classes ====================

    @Nested
    @DisplayName("findGetMethods() tests")
    class FindGetMethodsTests {

        @Test
        @DisplayName("Should find all getter methods")
        void shouldFindAllGetterMethods() {
            // Given
            SimpleObject obj = new SimpleObject();

            // When
            List<Method> getters = ReflectionUtil.findGetMethods(obj);

            // Then
            assertThat(getters)
                    .isNotEmpty()
                    .extracting(Method::getName)
                    .contains("getName", "getAge", "isActive");
        }

        @Test
        @DisplayName("Should identify boolean getters with 'is' prefix")
        void shouldIdentifyBooleanGetters() {
            // Given
            SimpleObject obj = new SimpleObject();

            // When
            List<Method> getters = ReflectionUtil.findGetMethods(obj);

            // Then
            assertThat(getters)
                    .extracting(Method::getName)
                    .anyMatch(name -> name.startsWith("is"));
        }

        @Test
        @DisplayName("Should return empty list for object without getters")
        void shouldReturnEmptyListForObjectWithoutGetters() {
            // Given
            NoGetterSetterObject obj = new NoGetterSetterObject();

            // When
            List<Method> getters = ReflectionUtil.findGetMethods(obj);

            // Then - May include Object's getters like getClass()
            assertThat(getters)
                    .extracting(Method::getName)
                    .allMatch(name -> name.toLowerCase().startsWith("get") ||
                            name.toLowerCase().startsWith("is"));
        }

        @Test
        @DisplayName("Should find inherited getter methods")
        void shouldFindInheritedGetterMethods() {
            // Given
            Child obj = new Child();

            // When
            List<Method> getters = ReflectionUtil.findGetMethods(obj);

            // Then
            assertThat(getters)
                    .extracting(Method::getName)
                    .contains("getParentField", "getChildField");
        }
    }

    @Nested
    @DisplayName("findSetMethods() tests")
    class FindSetMethodsTests {

        @Test
        @DisplayName("Should find all setter methods")
        void shouldFindAllSetterMethods() {
            // Given
            SimpleObject obj = new SimpleObject();

            // When
            List<Method> setters = ReflectionUtil.findSetMethods(obj);

            // Then
            assertThat(setters)
                    .isNotEmpty()
                    .extracting(Method::getName)
                    .contains("setName", "setAge", "setActive");
        }

        @Test
        @DisplayName("Should return empty list for object without setters")
        void shouldReturnEmptyListForObjectWithoutSetters() {
            // Given
            NoGetterSetterObject obj = new NoGetterSetterObject();

            // When
            List<Method> setters = ReflectionUtil.findSetMethods(obj);

            // Then
            assertThat(setters).isEmpty();
        }

        @Test
        @DisplayName("Should find inherited setter methods")
        void shouldFindInheritedSetterMethods() {
            // Given
            Child obj = new Child();

            // When
            List<Method> setters = ReflectionUtil.findSetMethods(obj);

            // Then
            assertThat(setters)
                    .extracting(Method::getName)
                    .contains("setParentField", "setChildField");
        }
    }

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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2);

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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2);

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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2);

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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2);

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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2);

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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2, filterNames, true);

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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2, filterNames, false);

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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2, null);

            // Then - Should compare all fields
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
            List<Method> filtered = ReflectionUtil.filterList(methods, filterNames, true);

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
            List<Method> filtered = ReflectionUtil.filterList(methods, filterNames, false);

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
            List<Method> filtered = ReflectionUtil.filterList(methods, filterNames, false);

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
            List<Method> filtered = ReflectionUtil.filterList(methods, filterNames, true);

            // Then - Nothing removed
            assertThat(filtered).hasSize(originalSize);
        }
    }

    @Nested
    @DisplayName("safeGet() tests")
    class SafeGetTests {

        @Test
        @DisplayName("Should return Optional with value when object and getter are valid")
        void shouldReturnOptionalWithValue() {
            // Given
            SimpleObject obj = new SimpleObject();

            // When
            Optional<String> result = ReflectionUtil.safeGet(obj, SimpleObject::getName);

            // Then
            assertThat(result)
                    .isPresent()
                    .contains("test");
        }

        @Test
        @DisplayName("Should return empty Optional when object is null")
        void shouldReturnEmptyOptionalWhenObjectIsNull() {
            // When
            Optional<String> result = ReflectionUtil.safeGet(null, SimpleObject::getName);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty Optional when getter returns null")
        void shouldReturnEmptyOptionalWhenGetterReturnsNull() {
            // Given
            ComparisonObject obj = new ComparisonObject(null, null, null, null);

            // When
            Optional<String> result = ReflectionUtil.safeGet(obj, ComparisonObject::getStringValue);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should work with method references")
        void shouldWorkWithMethodReferences() {
            // Given
            SimpleObject obj = new SimpleObject();

            // When
            Optional<Integer> result = ReflectionUtil.safeGet(obj, SimpleObject::getAge);

            // Then
            assertThat(result)
                    .isPresent()
                    .contains(25);
        }
    }

    @Nested
    @DisplayName("safeGetWithDefaultValue() tests")
    class SafeGetWithDefaultValueTests {

        @Test
        @DisplayName("Should return actual value when present")
        void shouldReturnActualValueWhenPresent() {
            // Given
            SimpleObject obj = new SimpleObject();

            // When
            String result = ReflectionUtil.safeGetWithDefaultValue(
                    obj,
                    SimpleObject::getName,
                    "default");

            // Then
            assertThat(result).isEqualTo("test");
        }

        @Test
        @DisplayName("Should return default value when object is null")
        void shouldReturnDefaultValueWhenObjectIsNull() {
            // When
            String result = ReflectionUtil.safeGetWithDefaultValue(
                    null,
                    SimpleObject::getName,
                    "default");

            // Then
            assertThat(result).isEqualTo("default");
        }

        @Test
        @DisplayName("Should return default value when getter returns null")
        void shouldReturnDefaultValueWhenGetterReturnsNull() {
            // Given
            ComparisonObject obj = new ComparisonObject(null, null, null, null);

            // When
            String result = ReflectionUtil.safeGetWithDefaultValue(
                    obj,
                    ComparisonObject::getStringValue,
                    "default");

            // Then
            assertThat(result).isEqualTo("default");
        }

        @Test
        @DisplayName("Should work with different types")
        void shouldWorkWithDifferentTypes() {
            // Given
            SimpleObject obj = new SimpleObject();

            // When
            Integer result = ReflectionUtil.safeGetWithDefaultValue(
                    obj,
                    SimpleObject::getAge,
                    0);

            // Then
            assertThat(result).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("removeNulls() tests")
    class RemoveNullsTests {

        @Test
        @DisplayName("Should remove null elements from list")
        void shouldRemoveNullElements() {
            // Given
            List<String> list = Arrays.asList("a", null, "b", null, "c");

            // When
            List<String> result = ReflectionUtil.removeNulls(list);

            // Then
            assertThat(result)
                    .hasSize(3)
                    .containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Should return empty list for null input")
        void shouldReturnEmptyListForNullInput() {
            // When
            List<String> result = ReflectionUtil.removeNulls(null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list for empty input")
        void shouldReturnEmptyListForEmptyInput() {
            // When
            List<String> result = ReflectionUtil.removeNulls(List.of());

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle list with all nulls")
        void shouldHandleListWithAllNulls() {
            // Given
            List<String> list = Arrays.asList(null, null, null);

            // When
            List<String> result = ReflectionUtil.removeNulls(list);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle list with no nulls")
        void shouldHandleListWithNoNulls() {
            // Given
            List<String> list = List.of("a", "b", "c");

            // When
            List<String> result = ReflectionUtil.removeNulls(list);

            // Then
            assertThat(result)
                    .hasSize(3)
                    .containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("Dynamic getter/setter invocation tests")
    class DynamicInvocationTests {

        @Test
        @DisplayName("Should get value dynamically through getter name from field")
        void shouldGetValueDynamicallyFromField() throws Exception {
            // Given
            SimpleObject obj = new SimpleObject();
            var field = SimpleObject.class.getDeclaredField("name");

            // When
            Object value = ReflectionUtil.getValueDynamicallyThroughGetterNameFromField(field, obj);

            // Then
            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("Should get value dynamically through getter name")
        void shouldGetValueDynamicallyThroughGetterName() {
            // Given
            SimpleObject obj = new SimpleObject();

            // When
            Object value = ReflectionUtil.getValueDynamicallyThroughGetterName("getName", obj);

            // Then
            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("Should set value dynamically through setter name from field")
        void shouldSetValueDynamicallyFromField() throws Exception {
            // Given
            SimpleObject obj = new SimpleObject();
            var field = SimpleObject.class.getDeclaredField("name");

            // When
            ReflectionUtil.setValueDynamicallyThroughSetterNameFromField(field, obj, "updated");

            // Then
            assertThat(obj.getName()).isEqualTo("updated");
        }

        @Test
        @DisplayName("Should set value dynamically through setter name")
        void shouldSetValueDynamicallyThroughSetterName() {
            // Given
            SimpleObject obj = new SimpleObject();

            // When
            ReflectionUtil.setValueDynamicallyThroughSetterName("setName", obj, "updated");

            // Then
            assertThat(obj.getName()).isEqualTo("updated");
        }

        @Test
        @DisplayName("Should handle boolean getter with 'is' prefix")
        void shouldHandleBooleanGetterWithIsPrefix() throws Exception {
            // Given
            SimpleObject obj = new SimpleObject();
            var field = SimpleObject.class.getDeclaredField("active");

            // When
            Object value = ReflectionUtil.getValueDynamicallyThroughGetterNameFromField(field, obj);

            // Then
            assertThat(value).isEqualTo(true);
        }

        @Test
        @DisplayName("Should throw ApiException when getter not found")
        void shouldThrowExceptionWhenGetterNotFound() {
            // Given
            SimpleObject obj = new SimpleObject();

            // When/Then
            assertThatThrownBy(() ->
                    ReflectionUtil.getValueDynamicallyThroughGetterName("getNonExistent", obj))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("There's no getter with specified name");
        }

        @Test
        @DisplayName("Should throw ApiException when setter not found")
        void shouldThrowExceptionWhenSetterNotFound() {
            // Given
            SimpleObject obj = new SimpleObject();

            // When/Then
            assertThatThrownBy(() ->
                    ReflectionUtil.setValueDynamicallyThroughSetterName("setNonExistent", obj, "value"))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("There's no setter with specified name");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle complex object comparison scenario")
        void shouldHandleComplexComparison() throws Exception {
            // Given
            ComparisonObject obj1 = new ComparisonObject("test", 42, 3.14, List.of("a", "b"));
            ComparisonObject obj2 = new ComparisonObject("test", 42, 3.14, List.of("a", "b"));

            // When
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should get all fields including inherited")
        void shouldGetAllFieldsIncludingInherited() {
            // Given
            Child obj = new Child();

            // When
            var fields = ReflectionUtil.getFieldsAsCollection(obj);

            // Then
            assertThat(fields)
                    .extracting(java.lang.reflect.Field::getName)
                    .contains("childField", "parentField");
        }

        @Test
        @DisplayName("Should get fields without including parents when specified")
        void shouldGetFieldsWithoutParents() {
            // Given
            Child obj = new Child();

            // When
            var fields = ReflectionUtil.getFieldsAsCollection(obj, false);

            // Then
            assertThat(fields)
                    .extracting(java.lang.reflect.Field::getName)
                    .contains("childField")
                    .doesNotContain("parentField");
        }

        @Test
        @DisplayName("Should handle primitive wrapper conversions in setter")
        void shouldHandlePrimitiveWrapperConversions() {
            // Given
            SimpleObject obj = new SimpleObject();

            // When - Setting Integer value
            ReflectionUtil.setValueDynamicallyThroughSetterName("setAge", obj, 30);

            // Then
            assertThat(obj.getAge()).isEqualTo(30);
        }
    }
}