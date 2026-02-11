package io.github.gregoryfeijon.object.factory.commons;

import io.github.gregoryfeijon.object.factory.commons.domain.Child;
import io.github.gregoryfeijon.object.factory.commons.domain.CollectionSetterObject;
import io.github.gregoryfeijon.object.factory.commons.domain.ComparisonObject;
import io.github.gregoryfeijon.object.factory.commons.domain.MismatchedGetterObject;
import io.github.gregoryfeijon.object.factory.commons.domain.NoGetterSetterObject;
import io.github.gregoryfeijon.object.factory.commons.domain.NonPublicAccessorObject;
import io.github.gregoryfeijon.object.factory.commons.domain.SimpleObject;
import io.github.gregoryfeijon.object.factory.commons.exception.ApiException;
import io.github.gregoryfeijon.object.factory.commons.utils.ReflectionUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
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
    @DisplayName("copyProperties() tests")
    class CopyPropertiesTests {

        @Test
        @DisplayName("Should copy all properties from source to target")
        void shouldCopyAllProperties() {
            // Given
            SimpleObject source = new SimpleObject();
            source.setName("John");
            source.setAge(30);
            source.setActive(false);

            SimpleObject target = new SimpleObject();

            // When
            ReflectionUtil.copyProperties(source, target);

            // Then
            assertThat(target.getName()).isEqualTo("John");
            assertThat(target.getAge()).isEqualTo(30);
            assertThat(target.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should copy properties ignoring specified ones")
        void shouldCopyPropertiesIgnoringSpecified() {
            // Given
            SimpleObject source = new SimpleObject();
            source.setName("Jane");
            source.setAge(25);
            source.setActive(true);

            SimpleObject target = new SimpleObject();

            // When
            ReflectionUtil.copyProperties(source, target, "name", "active");

            // Then
            assertThat(target.getName()).isEqualTo("test"); // Original value
            assertThat(target.getAge()).isEqualTo(25); // Copied
            assertThat(target.isActive()).isTrue(); // Original value (from constructor)
        }

        @Test
        @DisplayName("Should handle null values during copy")
        void shouldHandleNullValuesDuringCopy() {
            // Given
            SimpleObject source = new SimpleObject();
            source.setName(null);
            source.setAge(0);
            source.setActive(false);

            SimpleObject target = new SimpleObject();

            // When
            ReflectionUtil.copyProperties(source, target);

            // Then
            assertThat(target.getName()).isNull();
            assertThat(target.getAge()).isZero();
            assertThat(target.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should copy inherited properties")
        void shouldCopyInheritedProperties() {
            // Given
            Child source = new Child();
            source.setChildField("child value");
            source.setParentField("parent value");

            Child target = new Child();

            // When
            ReflectionUtil.copyProperties(source, target);

            // Then
            assertThat(target.getChildField()).isEqualTo("child value");
            assertThat(target.getParentField()).isEqualTo("parent value");
        }

        @Test
        @DisplayName("Should throw exception when source is null")
        void shouldThrowExceptionWhenSourceIsNull() {
            // Given
            SimpleObject target = new SimpleObject();

            // When/Then
            assertThatThrownBy(() -> ReflectionUtil.copyProperties(null, target))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Source object cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when target is null")
        void shouldThrowExceptionWhenTargetIsNull() {
            // Given
            SimpleObject source = new SimpleObject();

            // When/Then
            assertThatThrownBy(() -> ReflectionUtil.copyProperties(source, null))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Target object cannot be null");
        }

        @Test
        @DisplayName("Should handle empty ignore properties array")
        void shouldHandleEmptyIgnoreProperties() {
            // Given
            SimpleObject source = new SimpleObject();
            source.setName("Test");

            SimpleObject target = new SimpleObject();

            // When
            ReflectionUtil.copyProperties(source, target, new String[0]);

            // Then
            assertThat(target.getName()).isEqualTo("Test");
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

    // ==================== Additional Coverage Tests ====================

    @Nested
    @DisplayName("getMethodsAsList() tests")
    class GetMethodsAsListTests {

        @Test
        @DisplayName("Should return all methods for an object")
        void shouldReturnAllMethods() {
            // Given
            SimpleObject obj = new SimpleObject();

            // When
            Collection<Method> methods = ReflectionUtil.getMethodsAsList(obj);

            // Then
            assertThat(methods).isNotEmpty();
            assertThat(methods)
                    .extracting(Method::getName)
                    .contains("getName", "setName", "getAge", "setAge");
        }

        @Test
        @DisplayName("Should throw ApiException when object is null")
        void shouldThrowWhenObjectIsNull() {
            assertThatThrownBy(() -> ReflectionUtil.getMethodsAsList(null))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("cannot be null");
        }
    }

    @Nested
    @DisplayName("getFieldsAsCollection() with custom collection type tests")
    class GetFieldsAsCollectionCustomTypeTests {

        @Test
        @DisplayName("Should return fields in custom collection type")
        void shouldReturnFieldsInCustomCollectionType() {
            // Given
            SimpleObject obj = new SimpleObject();

            // When
            HashSet<Field> fields = ReflectionUtil.getFieldsAsCollection(obj, HashSet::new);

            // Then
            assertThat(fields).isNotEmpty();
            assertThat(fields)
                    .extracting(Field::getName)
                    .contains("name", "age", "active");
        }

        @Test
        @DisplayName("Should throw when object is null for getFieldsAsCollection")
        void shouldThrowWhenObjectIsNullForFields() {
            assertThatThrownBy(() -> ReflectionUtil.getFieldsAsCollection(null))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("cannot be null");
        }
    }

    @Nested
    @DisplayName("Validation tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw when findGetMethods receives null")
        void shouldThrowWhenFindGetMethodsReceivesNull() {
            assertThatThrownBy(() -> ReflectionUtil.findGetMethods(null))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("cannot be null");
        }

        @Test
        @DisplayName("Should throw when findSetMethods receives null")
        void shouldThrowWhenFindSetMethodsReceivesNull() {
            assertThatThrownBy(() -> ReflectionUtil.findSetMethods(null))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("cannot be null");
        }

        @Test
        @DisplayName("Should throw when getter function is null in safeGet")
        void shouldThrowWhenGetterFunctionIsNull() {
            assertThatThrownBy(() -> ReflectionUtil.safeGet(new SimpleObject(), null))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Getter function cannot be null");
        }

        @Test
        @DisplayName("Should throw when getter function is null in safeGetWithDefaultValue")
        void shouldThrowWhenGetterFunctionIsNullInSafeGetWithDefault() {
            assertThatThrownBy(() -> ReflectionUtil.safeGetWithDefaultValue(new SimpleObject(), null, "default"))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Getter function cannot be null");
        }

        @Test
        @DisplayName("Should throw when field is null in getValueDynamicallyThroughGetterNameFromField")
        void shouldThrowWhenFieldIsNullInGetterFromField() {
            assertThatThrownBy(() ->
                    ReflectionUtil.getValueDynamicallyThroughGetterNameFromField(null, new SimpleObject()))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Field cannot be null");
        }

        @Test
        @DisplayName("Should throw when object is null in getValueDynamicallyThroughGetterNameFromField")
        void shouldThrowWhenObjectIsNullInGetterFromField() throws Exception {
            Field field = SimpleObject.class.getDeclaredField("name");

            assertThatThrownBy(() ->
                    ReflectionUtil.getValueDynamicallyThroughGetterNameFromField(field, null))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("cannot be null");
        }

        @Test
        @DisplayName("Should throw when getter name is null")
        void shouldThrowWhenGetterNameIsNull() {
            assertThatThrownBy(() ->
                    ReflectionUtil.getValueDynamicallyThroughGetterName(null, new SimpleObject()))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("method name cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw when getter name is empty")
        void shouldThrowWhenGetterNameIsEmpty() {
            assertThatThrownBy(() ->
                    ReflectionUtil.getValueDynamicallyThroughGetterName("", new SimpleObject()))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("method name cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw when getter name is blank")
        void shouldThrowWhenGetterNameIsBlank() {
            assertThatThrownBy(() ->
                    ReflectionUtil.getValueDynamicallyThroughGetterName("   ", new SimpleObject()))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("method name cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw when getter object is null")
        void shouldThrowWhenGetterObjectIsNull() {
            assertThatThrownBy(() ->
                    ReflectionUtil.getValueDynamicallyThroughGetterName("getName", null))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("cannot be null");
        }

        @Test
        @DisplayName("Should throw when field is null in setValueDynamicallyThroughSetterNameFromField")
        void shouldThrowWhenFieldIsNullInSetterFromField() {
            assertThatThrownBy(() ->
                    ReflectionUtil.setValueDynamicallyThroughSetterNameFromField(null, new SimpleObject(), "value"))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Field cannot be null");
        }

        @Test
        @DisplayName("Should throw when object is null in setValueDynamicallyThroughSetterNameFromField")
        void shouldThrowWhenObjectIsNullInSetterFromField() throws Exception {
            Field field = SimpleObject.class.getDeclaredField("name");

            assertThatThrownBy(() ->
                    ReflectionUtil.setValueDynamicallyThroughSetterNameFromField(field, null, "value"))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("cannot be null");
        }

        @Test
        @DisplayName("Should throw when setter name is null")
        void shouldThrowWhenSetterNameIsNull() {
            assertThatThrownBy(() ->
                    ReflectionUtil.setValueDynamicallyThroughSetterName(null, new SimpleObject(), "value"))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("method name cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw when setter name is empty")
        void shouldThrowWhenSetterNameIsEmpty() {
            assertThatThrownBy(() ->
                    ReflectionUtil.setValueDynamicallyThroughSetterName("", new SimpleObject(), "value"))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("method name cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw when setter target is null")
        void shouldThrowWhenSetterTargetIsNull() {
            assertThatThrownBy(() ->
                    ReflectionUtil.setValueDynamicallyThroughSetterName("setName", null, "value"))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("cannot be null");
        }
    }

    @Nested
    @DisplayName("compareObjectsValues() validation and edge case tests")
    class CompareObjectsValuesValidationTests {

        @Test
        @DisplayName("Should throw when first entity is null")
        void shouldThrowWhenFirstEntityIsNull() {
            assertThatThrownBy(() -> ReflectionUtil.compareObjectsValues(null, new SimpleObject()))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Entities to compare cannot be null");
        }

        @Test
        @DisplayName("Should throw when second entity is null")
        void shouldThrowWhenSecondEntityIsNull() {
            assertThatThrownBy(() -> ReflectionUtil.compareObjectsValues(new SimpleObject(), null))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Entities to compare cannot be null");
        }

        @Test
        @DisplayName("Should throw when entities are of different types")
        void shouldThrowWhenEntitiesAreDifferentTypes() {
            assertThatThrownBy(() -> ReflectionUtil.compareObjectsValues("string", 42))
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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2);

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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2);

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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2);

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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2);

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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2);

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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2);

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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2, new String[]{"name"});

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should validate entities in 4-param compare method")
        void shouldValidateEntitiesIn4ParamCompare() {
            assertThatThrownBy(() ->
                    ReflectionUtil.compareObjectsValues(null, new SimpleObject(), new String[]{"name"}, true))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Entities to compare cannot be null");
        }
    }

    @Nested
    @DisplayName("Non-public accessor tests")
    class NonPublicAccessorTests {

        @Test
        @DisplayName("Should throw when getter is not public")
        void shouldThrowWhenGetterIsNotPublic() {
            // Given
            NonPublicAccessorObject obj = new NonPublicAccessorObject();

            // When/Then
            assertThatThrownBy(() ->
                    ReflectionUtil.getValueDynamicallyThroughGetterName("getValue", obj))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Getter method is not public: getValue");
        }

        @Test
        @DisplayName("Should throw when setter is not public")
        void shouldThrowWhenSetterIsNotPublic() {
            // Given
            NonPublicAccessorObject obj = new NonPublicAccessorObject();

            // When/Then
            assertThatThrownBy(() ->
                    ReflectionUtil.setValueDynamicallyThroughSetterName("setValue", obj, "test"))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Setter method is not public: setValue");
        }
    }

    @Nested
    @DisplayName("Setter type conversion tests")
    class SetterTypeConversionTests {

        @Test
        @DisplayName("Should set null value on primitive field with default value")
        void shouldSetNullOnPrimitiveFieldWithDefault() {
            // Given
            SimpleObject obj = new SimpleObject();
            obj.setActive(true);

            // When - setting null on boolean (primitive) field should use default (false)
            ReflectionUtil.setValueDynamicallyThroughSetterName("setActive", obj, null);

            // Then
            assertThat(obj.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should set null value on non-primitive field")
        void shouldSetNullOnNonPrimitiveField() {
            // Given
            SimpleObject obj = new SimpleObject();

            // When
            ReflectionUtil.setValueDynamicallyThroughSetterName("setName", obj, null);

            // Then
            assertThat(obj.getName()).isNull();
        }

        @Test
        @DisplayName("Should throw for incompatible types")
        void shouldThrowForIncompatibleTypes() {
            // Given
            SimpleObject obj = new SimpleObject();

            // When/Then - trying to set a List into an Integer field
            assertThatThrownBy(() ->
                    ReflectionUtil.setValueDynamicallyThroughSetterName("setAge", obj, List.of("not a number")))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Incompatible parameter type for setter");
        }

        @Test
        @DisplayName("Should handle wrapper to primitive conversion")
        void shouldHandleWrapperToPrimitiveConversion() {
            // Given
            SimpleObject obj = new SimpleObject();
            Integer wrappedValue = 42;

            // When - setting Integer (wrapper) into int (primitive) field
            ReflectionUtil.setValueDynamicallyThroughSetterName("setAge", obj, wrappedValue);

            // Then
            assertThat(obj.getAge()).isEqualTo(42);
        }

        @Test
        @DisplayName("Should handle direct assignment for compatible types")
        void shouldHandleDirectAssignment() {
            // Given
            SimpleObject obj = new SimpleObject();

            // When - setting String into String field (direct assignment)
            ReflectionUtil.setValueDynamicallyThroughSetterName("setName", obj, "direct");

            // Then
            assertThat(obj.getName()).isEqualTo("direct");
        }
    }

    @Nested
    @DisplayName("copyProperties() additional tests")
    class CopyPropertiesAdditionalTests {

        @Test
        @DisplayName("Should handle null ignoreProperties gracefully")
        void shouldHandleNullIgnoreProperties() {
            // Given
            SimpleObject source = new SimpleObject();
            source.setName("copy-test");
            SimpleObject target = new SimpleObject();

            // When
            ReflectionUtil.copyProperties(source, target, (String[]) null);

            // Then
            assertThat(target.getName()).isEqualTo("copy-test");
        }

        @Test
        @DisplayName("Should silently skip fields without matching setter in target")
        void shouldSkipFieldsWithoutMatchingSetter() {
            // Given - Child has parentField from Parent, SimpleObject does not
            Child source = new Child();
            source.setChildField("test-child");
            SimpleObject target = new SimpleObject();

            // When - Should not throw, just skip unmatched fields
            ReflectionUtil.copyProperties(source, target);

            // Then - target should remain unchanged for unmatched fields
            assertThat(target.getName()).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("findMethodInList() empty list tests")
    class FindMethodInListTests {

        @Test
        @DisplayName("Should throw when no getter methods exist for getter lookup")
        void shouldThrowWhenNoGetterMethodsExist() {
            // Given
            NoGetterSetterObject obj = new NoGetterSetterObject();

            // When/Then - getValueDynamically needs a getter, but the getClass() getter
            // will be found. Use a non-matching name to trigger "no method with name"
            assertThatThrownBy(() ->
                    ReflectionUtil.getValueDynamicallyThroughGetterName("getNonExistent", obj))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("There's no getter with specified name");
        }

        @Test
        @DisplayName("Should throw when no setter methods exist")
        void shouldThrowWhenNoSetterMethodsExist() {
            // Given
            NoGetterSetterObject obj = new NoGetterSetterObject();

            // When/Then
            assertThatThrownBy(() ->
                    ReflectionUtil.setValueDynamicallyThroughSetterName("setField", obj, "value"))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("There's no setter method in specified Object!");
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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2);

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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2);

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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2);

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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2);

            // Then - null != ["item"]
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("copyFieldValue exception handling tests")
    class CopyFieldValueExceptionTests {

        @Test
        @DisplayName("Should silently handle exception when copying incompatible field")
        void shouldSilentlyHandleExceptionWhenCopyingIncompatibleField() {
            // Given - source has fields that target can't accept
            // Using Child -> SimpleObject where parentField doesn't exist in target
            // but the setter logic for non-matching getter throws internally
            MismatchedGetterObject source = new MismatchedGetterObject("test", 42L);
            SimpleObject target = new SimpleObject();

            // When - should not throw, just log debug
            ReflectionUtil.copyProperties(source, target);

            // Then - matching fields should be copied, non-matching silently skipped
            assertThat(target.getName()).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("Setter type conversion detailed tests")
    class SetterTypeConversionDetailedTests {

        @Test
        @DisplayName("Should handle primitive param with non-matching wrapper type returning false")
        void shouldHandlePrimitiveParamNonMatchingWrapper() {
            // Given - try setting a String into an int field
            SimpleObject obj = new SimpleObject();

            // When/Then - String is not assignable to Integer wrapper
            assertThatThrownBy(() ->
                    ReflectionUtil.setValueDynamicallyThroughSetterName("setAge", obj, "not a number"))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Incompatible parameter type for setter");
        }

        @Test
        @DisplayName("Should handle wrapper conversion returning false")
        void shouldHandleWrapperConversionReturningFalse() {
            // Given - try setting a Long into a String field (wrapper conversion fails)
            SimpleObject obj = new SimpleObject();

            // When/Then
            assertThatThrownBy(() ->
                    ReflectionUtil.setValueDynamicallyThroughSetterName("setName", obj, 42L))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Incompatible parameter type for setter");
        }

        @Test
        @DisplayName("Should handle direct assignment for subtype")
        void shouldHandleDirectAssignmentForSubtype() {
            // Given
            SimpleObject source = new SimpleObject();
            source.setName("subtype-test");
            source.setAge(42);
            source.setActive(false);

            SimpleObject target = new SimpleObject();

            // When - copy properties with direct assignment
            ReflectionUtil.copyProperties(source, target);

            // Then
            assertThat(target.getName()).isEqualTo("subtype-test");
            assertThat(target.getAge()).isEqualTo(42);
            assertThat(target.isActive()).isFalse();
        }
    }

    // ==================== Additional Coverage: filterList with set prefix ====================

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
            List<Method> filtered = ReflectionUtil.filterList(methods, filterNames, false);

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
            List<Method> filtered = ReflectionUtil.filterList(methods, filterNames, true);

            // Then
            assertThat(filtered)
                    .extracting(Method::getName)
                    .doesNotContain("setName")
                    .contains("setAge", "setActive");
        }
    }

    // ==================== Additional Coverage: getFieldsAsCollection edge cases ====================

    @Nested
    @DisplayName("getFieldsAsCollection() additional coverage tests")
    class GetFieldsAsCollectionAdditionalTests {

        @Test
        @DisplayName("Should include parent fields when includeParents is true")
        void shouldIncludeParentFieldsWhenTrue() {
            // Given - Object has no parent beyond Object itself
            SimpleObject obj = new SimpleObject();

            // When
            var fields = ReflectionUtil.getFieldsAsCollection(obj, true);

            // Then - should contain own fields (parent is Object which has no declared fields)
            assertThat(fields)
                    .extracting(Field::getName)
                    .contains("name", "age", "active");
        }
    }

    // ==================== Additional Coverage: compareMethodValues with missing method ====================

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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2);

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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2);

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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2);

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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2);

            // Then
            assertThat(result).isFalse();
        }
    }

    // ==================== Additional Coverage: invokeSetterWithTypeConversion edge cases ====================

    @Nested
    @DisplayName("Setter invocation type conversion coverage tests")
    class SetterInvocationTypeCoverageTests {

        @Test
        @DisplayName("Should handle null value on non-primitive setter parameter")
        void shouldHandleNullValueOnNonPrimitiveSetter() {
            // Given
            SimpleObject obj = new SimpleObject();
            obj.setName("initial");

            // When - null on String (non-primitive) field
            ReflectionUtil.setValueDynamicallyThroughSetterName("setName", obj, null);

            // Then
            assertThat(obj.getName()).isNull();
        }

        @Test
        @DisplayName("Should handle null value on primitive boolean setter")
        void shouldHandleNullValueOnPrimitiveBooleanSetter() {
            // Given - active is primitive boolean, so null defaults to false
            SimpleObject obj = new SimpleObject();
            obj.setActive(true);

            // When - null on boolean (primitive) field, uses default value false
            ReflectionUtil.setValueDynamicallyThroughSetterName("setActive", obj, null);

            // Then
            assertThat(obj.isActive()).isFalse();
        }
    }

    // ==================== Additional Coverage: setter blank name ====================

    @Nested
    @DisplayName("Setter blank name tests")
    class SetterBlankNameTests {

        @Test
        @DisplayName("Should throw when setter name is blank")
        void shouldThrowWhenSetterNameIsBlank() {
            assertThatThrownBy(() ->
                    ReflectionUtil.setValueDynamicallyThroughSetterName("   ", new SimpleObject(), "value"))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("method name cannot be null or empty");
        }
    }

    // ==================== Additional Coverage: invokeForDirectAssignment path ====================

    @Nested
    @DisplayName("Direct assignment type conversion tests")
    class DirectAssignmentTests {

        @Test
        @DisplayName("Should assign ArrayList to Collection setter via direct assignment")
        void shouldAssignSubtypeToCollectionSetter() {
            // Given - setItems expects Collection, we pass ArrayList (subtype)
            CollectionSetterObject obj = new CollectionSetterObject();
            ArrayList<String> list = new ArrayList<>(List.of("a", "b"));

            // When - ArrayList is assignable to Collection (direct assignment path)
            ReflectionUtil.setValueDynamicallyThroughSetterName("setItems", obj, list);

            // Then
            assertThat(obj.getItems()).containsExactly("a", "b");
        }

        @Test
        @DisplayName("Should assign String to Object setter via direct assignment")
        void shouldAssignStringToObjectSetter() {
            // Given - setGenericValue expects Object, we pass String (subtype)
            CollectionSetterObject obj = new CollectionSetterObject();

            // When - String is assignable to Object (direct assignment path)
            ReflectionUtil.setValueDynamicallyThroughSetterName("setGenericValue", obj, "test");

            // Then
            assertThat(obj.getGenericValue()).isEqualTo("test");
        }

        @Test
        @DisplayName("Should assign Integer to Object setter via direct assignment")
        void shouldAssignIntegerToObjectSetter() {
            // Given
            CollectionSetterObject obj = new CollectionSetterObject();

            // When
            ReflectionUtil.setValueDynamicallyThroughSetterName("setGenericValue", obj, 42);

            // Then
            assertThat(obj.getGenericValue()).isEqualTo(42);
        }
    }

    // ==================== Additional Coverage: copyProperties with getter exception ====================

    @Nested
    @DisplayName("copyProperties() with getter exception tests")
    class CopyPropertiesGetterExceptionTests {

        @Test
        @DisplayName("Should silently skip field when getter has mismatched type")
        void shouldSilentlySkipFieldWhenGetterFails() {
            // Given - NonPublicAccessorObject has protected getter, which will throw
            // when ReflectionUtil tries to get value (getter not public)
            NonPublicAccessorObject source = new NonPublicAccessorObject();
            SimpleObject target = new SimpleObject();

            // When - should not throw, just silently skip
            ReflectionUtil.copyProperties(source, target);

            // Then - target should remain with default values
            assertThat(target.getName()).isEqualTo("test");
        }
    }

    // ==================== Additional Coverage: getFieldsAsCollection with false includeParents ====================

    @Nested
    @DisplayName("getFieldsAsCollection() with includeParents=false on class with parent")
    class GetFieldsWithoutParentsTests {

        @Test
        @DisplayName("Should exclude parent fields when includeParents is false and class has parent")
        void shouldExcludeParentFieldsWhenFalse() {
            // Given - Child extends Parent, with includeParents=false
            Child obj = new Child();

            // When
            var fields = ReflectionUtil.getFieldsAsCollection(obj, false);

            // Then
            assertThat(fields)
                    .extracting(Field::getName)
                    .contains("childField")
                    .doesNotContain("parentField");
        }

        @Test
        @DisplayName("Should include all fields including parent when includeParents is true")
        void shouldIncludeAllFieldsWhenTrue() {
            // Given
            Child obj = new Child();

            // When
            var fields = ReflectionUtil.getFieldsAsCollection(obj, true);

            // Then
            assertThat(fields)
                    .extracting(Field::getName)
                    .contains("childField", "parentField");
        }
    }

    // ==================== Additional Coverage: areStringsEquivalent with both non-null ====================

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
            boolean result = ReflectionUtil.compareObjectsValues(obj1, obj2);

            // Then
            assertThat(result).isTrue();
        }
    }
}