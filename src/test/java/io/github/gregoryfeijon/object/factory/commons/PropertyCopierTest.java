package io.github.gregoryfeijon.object.factory.commons;

import io.github.gregoryfeijon.object.factory.commons.domain.Child;
import io.github.gregoryfeijon.object.factory.commons.domain.MismatchedGetterObject;
import io.github.gregoryfeijon.object.factory.commons.domain.NonPublicAccessorObject;
import io.github.gregoryfeijon.object.factory.commons.domain.SimpleObject;
import io.github.gregoryfeijon.object.factory.commons.exception.ApiException;
import io.github.gregoryfeijon.object.factory.commons.utils.PropertyCopier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PropertyCopier Tests")
class PropertyCopierTest {

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
            PropertyCopier.copyProperties(source, target);

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
            PropertyCopier.copyProperties(source, target, "name", "active");

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
            PropertyCopier.copyProperties(source, target);

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
            PropertyCopier.copyProperties(source, target);

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
            assertThatThrownBy(() -> PropertyCopier.copyProperties(null, target))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Source object cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when target is null")
        void shouldThrowExceptionWhenTargetIsNull() {
            // Given
            SimpleObject source = new SimpleObject();

            // When/Then
            assertThatThrownBy(() -> PropertyCopier.copyProperties(source, null))
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
            PropertyCopier.copyProperties(source, target, new String[0]);

            // Then
            assertThat(target.getName()).isEqualTo("Test");
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
            PropertyCopier.copyProperties(source, target);

            // Then
            assertThat(target.getName()).isEqualTo("subtype-test");
            assertThat(target.getAge()).isEqualTo(42);
            assertThat(target.isActive()).isFalse();
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
            PropertyCopier.copyProperties(source, target, (String[]) null);

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
            PropertyCopier.copyProperties(source, target);

            // Then - target should remain unchanged for unmatched fields
            assertThat(target.getName()).isEqualTo("test");
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
            PropertyCopier.copyProperties(source, target);

            // Then - matching fields should be copied, non-matching silently skipped
            assertThat(target.getName()).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("copyProperties() with getter exception tests")
    class CopyPropertiesGetterExceptionTests {

        @Test
        @DisplayName("Should silently skip field when getter has mismatched type")
        void shouldSilentlySkipFieldWhenGetterFails() {
            // Given - NonPublicAccessorObject has protected getter, which will throw
            // when the accessor util tries to get value (getter not public)
            NonPublicAccessorObject source = new NonPublicAccessorObject();
            SimpleObject target = new SimpleObject();

            // When - should not throw, just silently skip
            PropertyCopier.copyProperties(source, target);

            // Then - target should remain with default values
            assertThat(target.getName()).isEqualTo("test");
        }
    }
}
