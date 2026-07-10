package io.github.gregoryfeijon.object.factory.commons;

import io.github.gregoryfeijon.object.factory.commons.domain.CollectionSetterObject;
import io.github.gregoryfeijon.object.factory.commons.domain.NoGetterSetterObject;
import io.github.gregoryfeijon.object.factory.commons.domain.NonPublicAccessorObject;
import io.github.gregoryfeijon.object.factory.commons.domain.SimpleObject;
import io.github.gregoryfeijon.object.factory.commons.exception.ApiException;
import io.github.gregoryfeijon.object.factory.commons.utils.DynamicAccessorUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DynamicAccessorUtil Tests")
class DynamicAccessorUtilTest {

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
            Object value = DynamicAccessorUtil.getValueDynamicallyThroughGetterNameFromField(field, obj);

            // Then
            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("Should get value dynamically through getter name")
        void shouldGetValueDynamicallyThroughGetterName() {
            // Given
            SimpleObject obj = new SimpleObject();

            // When
            Object value = DynamicAccessorUtil.getValueDynamicallyThroughGetterName("getName", obj);

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
            DynamicAccessorUtil.setValueDynamicallyThroughSetterNameFromField(field, obj, "updated");

            // Then
            assertThat(obj.getName()).isEqualTo("updated");
        }

        @Test
        @DisplayName("Should set value dynamically through setter name")
        void shouldSetValueDynamicallyThroughSetterName() {
            // Given
            SimpleObject obj = new SimpleObject();

            // When
            DynamicAccessorUtil.setValueDynamicallyThroughSetterName("setName", obj, "updated");

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
            Object value = DynamicAccessorUtil.getValueDynamicallyThroughGetterNameFromField(field, obj);

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
                    DynamicAccessorUtil.getValueDynamicallyThroughGetterName("getNonExistent", obj))
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
                    DynamicAccessorUtil.setValueDynamicallyThroughSetterName("setNonExistent", obj, "value"))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("There's no setter with specified name");
        }
    }

    @Nested
    @DisplayName("Validation tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw when field is null in getValueDynamicallyThroughGetterNameFromField")
        void shouldThrowWhenFieldIsNullInGetterFromField() {
            SimpleObject obj = new SimpleObject();
            assertThatThrownBy(() ->
                    DynamicAccessorUtil.getValueDynamicallyThroughGetterNameFromField(null, obj))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Field cannot be null");
        }

        @Test
        @DisplayName("Should throw when object is null in getValueDynamicallyThroughGetterNameFromField")
        void shouldThrowWhenObjectIsNullInGetterFromField() throws Exception {
            Field field = SimpleObject.class.getDeclaredField("name");

            assertThatThrownBy(() ->
                    DynamicAccessorUtil.getValueDynamicallyThroughGetterNameFromField(field, null))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("cannot be null");
        }

        @ParameterizedTest(name = "Should throw when getter name is ''{0}''")
        @NullSource
        @ValueSource(strings = {"", "   "})
        @DisplayName("Should throw when getter name is null, empty or blank")
        void shouldThrowWhenGetterNameIsInvalid(String name) {
            SimpleObject obj = new SimpleObject();
            assertThatThrownBy(() ->
                    DynamicAccessorUtil.getValueDynamicallyThroughGetterName(name, obj))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("method name cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw when getter object is null")
        void shouldThrowWhenGetterObjectIsNull() {
            assertThatThrownBy(() ->
                    DynamicAccessorUtil.getValueDynamicallyThroughGetterName("getName", null))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("cannot be null");
        }

        @Test
        @DisplayName("Should throw when field is null in setValueDynamicallyThroughSetterNameFromField")
        void shouldThrowWhenFieldIsNullInSetterFromField() {
            SimpleObject obj = new SimpleObject();
            assertThatThrownBy(() ->
                    DynamicAccessorUtil.setValueDynamicallyThroughSetterNameFromField(null, obj, "value"))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Field cannot be null");
        }

        @Test
        @DisplayName("Should throw when object is null in setValueDynamicallyThroughSetterNameFromField")
        void shouldThrowWhenObjectIsNullInSetterFromField() throws Exception {
            Field field = SimpleObject.class.getDeclaredField("name");

            assertThatThrownBy(() ->
                    DynamicAccessorUtil.setValueDynamicallyThroughSetterNameFromField(field, null, "value"))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("cannot be null");
        }

        @Test
        @DisplayName("Should throw when setter name is null")
        void shouldThrowWhenSetterNameIsNull() {
            SimpleObject obj = new SimpleObject();
            assertThatThrownBy(() ->
                    DynamicAccessorUtil.setValueDynamicallyThroughSetterName(null, obj, "value"))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("method name cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw when setter name is empty")
        void shouldThrowWhenSetterNameIsEmpty() {
            SimpleObject obj = new SimpleObject();
            assertThatThrownBy(() ->
                    DynamicAccessorUtil.setValueDynamicallyThroughSetterName("", obj, "value"))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("method name cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw when setter target is null")
        void shouldThrowWhenSetterTargetIsNull() {
            assertThatThrownBy(() ->
                    DynamicAccessorUtil.setValueDynamicallyThroughSetterName("setName", null, "value"))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("cannot be null");
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
                    DynamicAccessorUtil.getValueDynamicallyThroughGetterName("getValue", obj))
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
                    DynamicAccessorUtil.setValueDynamicallyThroughSetterName("setValue", obj, "test"))
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
            DynamicAccessorUtil.setValueDynamicallyThroughSetterName("setActive", obj, null);

            // Then
            assertThat(obj.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should set null value on non-primitive field")
        void shouldSetNullOnNonPrimitiveField() {
            // Given
            SimpleObject obj = new SimpleObject();

            // When
            DynamicAccessorUtil.setValueDynamicallyThroughSetterName("setName", obj, null);

            // Then
            assertThat(obj.getName()).isNull();
        }

        @Test
        @DisplayName("Should throw for incompatible types")
        void shouldThrowForIncompatibleTypes() {
            // Given
            SimpleObject obj = new SimpleObject();
            List<String> incompatibleValue = List.of("not a number");

            // When/Then - trying to set a List into an Integer field
            assertThatThrownBy(() ->
                    DynamicAccessorUtil.setValueDynamicallyThroughSetterName("setAge", obj, incompatibleValue))
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
            DynamicAccessorUtil.setValueDynamicallyThroughSetterName("setAge", obj, wrappedValue);

            // Then
            assertThat(obj.getAge()).isEqualTo(42);
        }

        @Test
        @DisplayName("Should handle direct assignment for compatible types")
        void shouldHandleDirectAssignment() {
            // Given
            SimpleObject obj = new SimpleObject();

            // When - setting String into String field (direct assignment)
            DynamicAccessorUtil.setValueDynamicallyThroughSetterName("setName", obj, "direct");

            // Then
            assertThat(obj.getName()).isEqualTo("direct");
        }

        @Test
        @DisplayName("Should handle primitive wrapper conversions in setter")
        void shouldHandlePrimitiveWrapperConversions() {
            // Given
            SimpleObject obj = new SimpleObject();

            // When - Setting Integer value
            DynamicAccessorUtil.setValueDynamicallyThroughSetterName("setAge", obj, 30);

            // Then
            assertThat(obj.getAge()).isEqualTo(30);
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
                    DynamicAccessorUtil.getValueDynamicallyThroughGetterName("getNonExistent", obj))
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
                    DynamicAccessorUtil.setValueDynamicallyThroughSetterName("setField", obj, "value"))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("There's no setter method in specified Object!");
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
                    DynamicAccessorUtil.setValueDynamicallyThroughSetterName("setAge", obj, "not a number"))
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
                    DynamicAccessorUtil.setValueDynamicallyThroughSetterName("setName", obj, 42L))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Incompatible parameter type for setter");
        }
    }

    @Nested
    @DisplayName("Setter blank name tests")
    class SetterBlankNameTests {

        @Test
        @DisplayName("Should throw when setter name is blank")
        void shouldThrowWhenSetterNameIsBlank() {
            SimpleObject obj = new SimpleObject();
            assertThatThrownBy(() ->
                    DynamicAccessorUtil.setValueDynamicallyThroughSetterName("   ", obj, "value"))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("method name cannot be null or empty");
        }
    }

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
            DynamicAccessorUtil.setValueDynamicallyThroughSetterName("setItems", obj, list);

            // Then
            assertThat(obj.getItems()).containsExactly("a", "b");
        }

        @Test
        @DisplayName("Should assign String to Object setter via direct assignment")
        void shouldAssignStringToObjectSetter() {
            // Given - setGenericValue expects Object, we pass String (subtype)
            CollectionSetterObject obj = new CollectionSetterObject();

            // When - String is assignable to Object (direct assignment path)
            DynamicAccessorUtil.setValueDynamicallyThroughSetterName("setGenericValue", obj, "test");

            // Then
            assertThat(obj.getGenericValue()).isEqualTo("test");
        }

        @Test
        @DisplayName("Should assign Integer to Object setter via direct assignment")
        void shouldAssignIntegerToObjectSetter() {
            // Given
            CollectionSetterObject obj = new CollectionSetterObject();

            // When
            DynamicAccessorUtil.setValueDynamicallyThroughSetterName("setGenericValue", obj, 42);

            // Then
            assertThat(obj.getGenericValue()).isEqualTo(42);
        }
    }
}
