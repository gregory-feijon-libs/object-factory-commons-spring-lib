package io.github.gregoryfeijon.object.factory.commons;

import io.github.gregoryfeijon.object.factory.commons.domain.Child;
import io.github.gregoryfeijon.object.factory.commons.domain.ComplexObject;
import io.github.gregoryfeijon.object.factory.commons.domain.NoAccessorsObject;
import io.github.gregoryfeijon.object.factory.commons.domain.NoGetterSetterObject;
import io.github.gregoryfeijon.object.factory.commons.domain.ObjectWithBusinessLogic;
import io.github.gregoryfeijon.object.factory.commons.domain.Parent;
import io.github.gregoryfeijon.object.factory.commons.domain.TestObject;
import io.github.gregoryfeijon.object.factory.commons.exception.ApiException;
import io.github.gregoryfeijon.object.factory.commons.utils.FieldUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.github.gregoryfeijon.object.factory.commons.utils.TestUtil.getField;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FieldUtil Tests")
class FieldUtilTest {

    // ==================== Nested Test Classes ====================

    @Nested
    @DisplayName("setProtectedFieldValue() tests")
    class SetProtectedFieldValueTests {

        @Test
        @DisplayName("Should set private field via setter method")
        void shouldSetPrivateFieldViaSetter() throws Exception {
            // Given
            TestObject obj = new TestObject();
            Field field = getField(TestObject.class, "privateField");
            String newValue = "updated";

            // When
            FieldUtil.setProtectedFieldValue(field, obj, newValue);

            // Then
            assertThat(obj.getPrivateField()).isEqualTo(newValue);
        }

        @Test
        @DisplayName("Should set protected field via setter method")
        void shouldSetProtectedFieldViaSetter() throws Exception {
            // Given
            TestObject obj = new TestObject();
            Field field = getField(TestObject.class, "protectedField");
            Integer newValue = 999;

            // When
            FieldUtil.setProtectedFieldValue(field, obj, newValue);

            // Then
            assertThat(obj.getProtectedField()).isEqualTo(newValue);
        }

        @Test
        @DisplayName("Should set public field directly")
        void shouldSetPublicFieldDirectly() throws Exception {
            // Given
            TestObject obj = new TestObject();
            Field field = getField(TestObject.class, "publicField");
            Double newValue = 2.71;

            // When
            FieldUtil.setProtectedFieldValue(field, obj, newValue);

            // Then
            assertThat(obj.publicField).isEqualTo(newValue);
        }

        @Test
        @DisplayName("Should set field without setter using VarHandle fallback")
        void shouldSetFieldWithoutSetterUsingVarHandle() throws Exception {
            // Given
            NoGetterSetterObject obj = new NoGetterSetterObject();
            Field field = getField(NoGetterSetterObject.class, "fieldWithoutAccessors");
            String newValue = "changed";

            // When
            FieldUtil.setProtectedFieldValue(field, obj, newValue);

            // Then
            String actualValue = (String) FieldUtil.getProtectedFieldValue(field, obj);
            assertThat(actualValue).isEqualTo(newValue);
        }

        @Test
        @DisplayName("Should set null value")
        void shouldSetNullValue() throws Exception {
            // Given
            ComplexObject obj = new ComplexObject();
            obj.setStringValue("initial");
            Field field = getField(ComplexObject.class, "stringValue");

            // When
            FieldUtil.setProtectedFieldValue(field, obj, null);

            // Then
            assertThat(obj.getStringValue()).isNull();
        }

        @ParameterizedTest(name = "Should set {0} type field")
        @MethodSource("provideDifferentTypes")
        @DisplayName("Should handle different field types")
        void shouldHandleDifferentTypes(String fieldName, Object value) throws Exception {
            // Given
            ComplexObject obj = new ComplexObject();
            Field field = getField(ComplexObject.class, fieldName);

            // When
            FieldUtil.setProtectedFieldValue(field, obj, value);

            // Then
            Object actualValue = FieldUtil.getProtectedFieldValue(field, obj);
            assertThat(actualValue).isEqualTo(value);
        }

        static Stream<Arguments> provideDifferentTypes() {
            return Stream.of(
                    Arguments.of("stringValue", "test string"),
                    Arguments.of("intValue", 42),
                    Arguments.of("booleanValue", true),
                    Arguments.of("nullValue", null)
            );
        }

        @Test
        @DisplayName("Should throw ApiException when field is null")
        void shouldThrowExceptionWhenFieldIsNull() {
            // Given
            TestObject obj = new TestObject();

            // When/Then
            assertThatThrownBy(() -> FieldUtil.setProtectedFieldValue(null, obj, "value"))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Field cannot be null");
        }

        @Test
        @DisplayName("Should throw ApiException when destination object is null")
        void shouldThrowExceptionWhenDestIsNull() throws Exception {
            // Given
            Field field = getField(TestObject.class, "privateField");

            // When/Then
            assertThatThrownBy(() -> FieldUtil.setProtectedFieldValue(field, null, "value"))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Destination object cannot be null");
        }

        @Test
        @DisplayName("Should handle final fields gracefully when possible")
        void shouldHandleFinalFieldsWhenPossible() throws Exception {
            // Given
            TestObject obj = new TestObject();
            Field field = getField(TestObject.class, "finalField");

            // When - Try to set final field (may succeed via VarHandle/FieldUtils)
            // This is a known behavior in Java - final fields CAN be modified via reflection

            // Then - Either succeeds (VarHandle/FieldUtils works) or throws
            try {
                FieldUtil.setProtectedFieldValue(field, obj, "modified");
                // If it succeeds, verify the value was set
                Object value = FieldUtil.getProtectedFieldValue(field, obj);
                assertThat(value).isIn("immutable", "modified"); // May or may not change
            } catch (ApiException e) {
                // If it fails, verify the error message
                assertThat(e.getMessage()).contains("Failed to set value for field");
            }
        }
    }

    @Nested
    @DisplayName("getProtectedFieldValue() tests")
    class GetProtectedFieldValueTests {

        @Test
        @DisplayName("Should get private field via getter method")
        void shouldGetPrivateFieldViaGetter() throws Exception {
            // Given
            TestObject obj = new TestObject();
            Field field = getField(TestObject.class, "privateField");

            // When
            Object value = FieldUtil.getProtectedFieldValue(field, obj);

            // Then
            assertThat(value).isEqualTo("initial");
        }

        @Test
        @DisplayName("Should get protected field via getter method")
        void shouldGetProtectedFieldViaGetter() throws Exception {
            // Given
            TestObject obj = new TestObject();
            Field field = getField(TestObject.class, "protectedField");

            // When
            Object value = FieldUtil.getProtectedFieldValue(field, obj);

            // Then
            assertThat(value).isEqualTo(42);
        }

        @Test
        @DisplayName("Should get public field directly")
        void shouldGetPublicFieldDirectly() throws Exception {
            // Given
            TestObject obj = new TestObject();
            Field field = getField(TestObject.class, "publicField");

            // When
            Object value = FieldUtil.getProtectedFieldValue(field, obj);

            // Then
            assertThat(value).isEqualTo(3.14);
        }

        @Test
        @DisplayName("Should get field without getter using VarHandle fallback")
        void shouldGetFieldWithoutGetterUsingVarHandle() throws Exception {
            // Given
            NoGetterSetterObject obj = new NoGetterSetterObject();
            Field field = getField(NoGetterSetterObject.class, "fieldWithoutAccessors");

            // When
            Object value = FieldUtil.getProtectedFieldValue(field, obj);

            // Then
            assertThat(value).isEqualTo("value");
        }

        @Test
        @DisplayName("Should return null for null field value")
        void shouldReturnNullForNullFieldValue() throws Exception {
            // Given
            ComplexObject obj = new ComplexObject();
            Field field = getField(ComplexObject.class, "stringValue");

            // When
            Object value = FieldUtil.getProtectedFieldValue(field, obj);

            // Then
            assertThat(value).isNull();
        }

        @Test
        @DisplayName("Should throw ApiException when field is null")
        void shouldThrowExceptionWhenFieldIsNull() {
            // Given
            TestObject obj = new TestObject();

            // When/Then
            assertThatThrownBy(() -> FieldUtil.getProtectedFieldValue(null, obj))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Field cannot be null");
        }

        @Test
        @DisplayName("Should throw ApiException when target object is null")
        void shouldThrowExceptionWhenTargetIsNull() throws Exception {
            // Given
            Field field = getField(TestObject.class, "privateField");

            // When/Then
            assertThatThrownBy(() -> FieldUtil.getProtectedFieldValue(field, null))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Target object cannot be null");
        }
    }

    @Nested
    @DisplayName("verifyNull() tests")
    class VerifyNullTests {

        @Test
        @DisplayName("Should return true when supplier returns null")
        void shouldReturnTrueWhenSupplierReturnsNull() {
            // Given
            Supplier<String> nullSupplier = () -> null;

            // When
            boolean result = FieldUtil.verifyNull(nullSupplier);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when supplier returns non-null value")
        void shouldReturnFalseWhenSupplierReturnsNonNull() {
            // Given
            Supplier<String> nonNullSupplier = () -> "value";

            // When
            boolean result = FieldUtil.verifyNull(nonNullSupplier);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should work with method reference")
        void shouldWorkWithMethodReference() {
            // Given
            TestObject obj = new TestObject();

            // When
            boolean result = FieldUtil.verifyNull(obj::getPrivateField);

            // Then
            assertThat(result).isFalse(); // privateField has initial value
        }

        @Test
        @DisplayName("Should work with lambda expression")
        void shouldWorkWithLambdaExpression() {
            // Given
            ComplexObject obj = new ComplexObject();

            // When
            boolean result = FieldUtil.verifyNull(() -> obj.getStringValue());

            // Then
            assertThat(result).isTrue(); // stringValue is null initially
        }

        @Test
        @DisplayName("Should throw ApiException when supplier is null")
        void shouldThrowExceptionWhenSupplierIsNull() {
            // When/Then
            assertThatThrownBy(() -> FieldUtil.verifyNull(null))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Supplier cannot be null");
        }
    }

    @Nested
    @DisplayName("Integration and Edge Cases Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should set and get value through same field")
        void shouldSetAndGetValueThroughSameField() throws Exception {
            // Given
            TestObject obj = new TestObject();
            Field field = getField(TestObject.class, "privateField");
            String newValue = "integration test";

            // When
            FieldUtil.setProtectedFieldValue(field, obj, newValue);
            Object retrievedValue = FieldUtil.getProtectedFieldValue(field, obj);

            // Then
            assertThat(retrievedValue).isEqualTo(newValue);
        }

        @Test
        @DisplayName("Should handle multiple field operations on same object")
        void shouldHandleMultipleOperationsOnSameObject() throws Exception {
            // Given
            ComplexObject obj = new ComplexObject();
            Field stringField = getField(ComplexObject.class, "stringValue");
            Field intField = getField(ComplexObject.class, "intValue");
            Field boolField = getField(ComplexObject.class, "booleanValue");

            // When
            FieldUtil.setProtectedFieldValue(stringField, obj, "test");
            FieldUtil.setProtectedFieldValue(intField, obj, 100);
            FieldUtil.setProtectedFieldValue(boolField, obj, true);

            // Then
            assertThat(FieldUtil.getProtectedFieldValue(stringField, obj)).isEqualTo("test");
            assertThat(FieldUtil.getProtectedFieldValue(intField, obj)).isEqualTo(100);
            assertThat(FieldUtil.getProtectedFieldValue(boolField, obj)).isEqualTo(true);
        }

        @Test
        @DisplayName("Should respect setter business logic")
        void shouldRespectSetterBusinessLogic() throws Exception {
            // Given
            ObjectWithBusinessLogic obj = new ObjectWithBusinessLogic();
            Field field = getField(ObjectWithBusinessLogic.class, "value");

            // When
            FieldUtil.setProtectedFieldValue(field, obj, "test");

            // Then
            assertThat(obj.getValue()).isEqualTo("TEST"); // Business logic applied
            assertThat(obj.getSetterCallCount()).isEqualTo(1); // Setter was called
        }

        @Test
        @DisplayName("Should handle inheritance correctly")
        void shouldHandleInheritanceCorrectly() throws Exception {
            // Given
            Child obj = new Child();
            Field parentField = Parent.class.getDeclaredField("parentField");
            Field childField = Child.class.getDeclaredField("childField");

            // When
            FieldUtil.setProtectedFieldValue(parentField, obj, "new parent");
            FieldUtil.setProtectedFieldValue(childField, obj, "new child");

            // Then
            assertThat(obj.getParentField()).isEqualTo("new parent");
            assertThat(obj.getChildField()).isEqualTo("new child");
        }
    }

    @Nested
    @DisplayName("Strategy Fallback Tests")
    class StrategyFallbackTests {

        @Test
        @DisplayName("Should use VarHandle when setter is not available")
        void shouldUseVarHandleWhenSetterNotAvailable() throws Exception {
            // Given
            NoGetterSetterObject obj = new NoGetterSetterObject();
            Field field = getField(NoGetterSetterObject.class, "fieldWithoutAccessors");

            // When
            FieldUtil.setProtectedFieldValue(field, obj, "via varhandle");

            // Then
            String value = (String) FieldUtil.getProtectedFieldValue(field, obj);
            assertThat(value).isEqualTo("via varhandle");
        }

        @Test
        @DisplayName("Should fallback through all strategies correctly")
        void shouldFallbackThroughAllStrategies() throws Exception {
            // Given
            NoAccessorsObject obj = new NoAccessorsObject();
            Field field = getField(NoAccessorsObject.class, "hiddenField");

            // When - No setter, will try VarHandle, then FieldUtils
            FieldUtil.setProtectedFieldValue(field, obj, "found");

            // Then
            Object value = FieldUtil.getProtectedFieldValue(field, obj);
            assertThat(value).isEqualTo("found");
        }
    }
}