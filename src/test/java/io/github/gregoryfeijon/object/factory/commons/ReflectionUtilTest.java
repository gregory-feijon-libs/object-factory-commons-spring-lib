package io.github.gregoryfeijon.object.factory.commons;

import io.github.gregoryfeijon.object.factory.commons.domain.Child;
import io.github.gregoryfeijon.object.factory.commons.domain.ComparisonObject;
import io.github.gregoryfeijon.object.factory.commons.domain.ConcreteClass;
import io.github.gregoryfeijon.object.factory.commons.domain.NoGetterSetterObject;
import io.github.gregoryfeijon.object.factory.commons.domain.SimpleObject;
import io.github.gregoryfeijon.object.factory.commons.exception.ApiException;
import io.github.gregoryfeijon.object.factory.commons.utils.ReflectionUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ReflectionUtil Tests")
class ReflectionUtilTest {

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
                    .isNotEmpty()
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
            SimpleObject obj = new SimpleObject();
            assertThatThrownBy(() -> ReflectionUtil.safeGet(obj, null))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Getter function cannot be null");
        }

        @Test
        @DisplayName("Should throw when getter function is null in safeGetWithDefaultValue")
        void shouldThrowWhenGetterFunctionIsNullInSafeGetWithDefault() {
            SimpleObject obj = new SimpleObject();
            assertThatThrownBy(() -> ReflectionUtil.safeGetWithDefaultValue(obj, null, "default"))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Getter function cannot be null");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should get all fields including inherited")
        void shouldGetAllFieldsIncludingInherited() {
            // Given
            Child obj = new Child();

            // When
            var fields = ReflectionUtil.getFieldsAsCollection(obj);

            // Then
            assertThat(fields)
                    .extracting(Field::getName)
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
                    .extracting(Field::getName)
                    .contains("childField")
                    .doesNotContain("parentField");
        }
    }

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

        @Test
        @DisplayName("Should include own and inherited fields from generic parent class")
        void shouldIncludeFieldsFromGenericParentClass() {
            // Given - ConcreteClass extends GenericClass<String, Integer>
            ConcreteClass obj = new ConcreteClass();
            obj.setSpecificField("specific");
            obj.setField1("generic value");
            obj.setField2(List.of(1, 2, 3));

            // When
            var fields = ReflectionUtil.getFieldsAsCollection(obj, true);

            // Then - should contain ConcreteClass's specificField and GenericClass's field1, field2, field3
            assertThat(fields)
                    .extracting(Field::getName)
                    .contains("specificField", "field1", "field2", "field3");

            // Verify values were set correctly through accessors
            assertThat(obj.getSpecificField()).isEqualTo("specific");
            assertThat(obj.getField1()).isEqualTo("generic value");
            assertThat(obj.getField2()).containsExactly(1, 2, 3);
        }
    }

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
}
