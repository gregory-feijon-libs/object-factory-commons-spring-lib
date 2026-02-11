package io.github.gregoryfeijon.object.factory.commons;

import io.github.gregoryfeijon.object.factory.commons.config.FactoryUtilTestConfig;
import io.github.gregoryfeijon.object.factory.commons.exception.ApiException;
import io.github.gregoryfeijon.object.factory.commons.service.AnotherTestService;
import io.github.gregoryfeijon.object.factory.commons.service.TestServiceImpl;
import io.github.gregoryfeijon.object.factory.commons.service.TestServiceInterface;
import io.github.gregoryfeijon.object.factory.commons.utils.factory.FactoryUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(FactoryUtilTestConfig.class)
@DisplayName("FactoryUtil Tests")
class FactoryUtilTest {

    @Autowired
    private ApplicationContext applicationContext;

    private ApplicationContext savedContext;

    @BeforeEach
    void saveContext() throws Exception {
        savedContext = getContextRef();
    }

    @AfterEach
    void restoreContext() throws Exception {
        setContextRef(savedContext);
    }

    // ==================== Reflection helpers for static AtomicReference ====================

    @SuppressWarnings("unchecked")
    private static AtomicReference<ApplicationContext> getAtomicRef() throws Exception {
        Field field = FactoryUtil.class.getDeclaredField("contextRef");
        field.setAccessible(true);
        return (AtomicReference<ApplicationContext>) field.get(null);
    }

    private static ApplicationContext getContextRef() throws Exception {
        return getAtomicRef().get();
    }

    private static void setContextRef(ApplicationContext ctx) throws Exception {
        getAtomicRef().set(ctx);
    }

    // ==================== getBean(Class) ====================

    @Nested
    @DisplayName("getBean(Class) tests")
    class GetBeanTests {

        @Test
        @DisplayName("Should retrieve bean by type")
        void shouldRetrieveBeanByType() {
            TestServiceInterface service = FactoryUtil.getBean(TestServiceInterface.class);

            assertThat(service).isNotNull();
            assertThat(service.execute()).isEqualTo("Executed: test");
        }

        @Test
        @DisplayName("Should throw ApiException when bean class is null")
        void shouldThrowWhenBeanClassIsNull() {
            assertThatThrownBy(() -> FactoryUtil.getBean(null))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Bean class cannot be null");
        }

        @Test
        @DisplayName("Should throw ApiException when bean is not found")
        void shouldThrowWhenBeanNotFound() {
            assertThatThrownBy(() -> FactoryUtil.getBean(Runnable.class))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Failed to retrieve bean of type:");
        }

        @Test
        @DisplayName("Should throw ApiException when context is not initialized")
        void shouldThrowWhenContextNotInitialized() throws Exception {
            setContextRef(null);

            assertThatThrownBy(() -> FactoryUtil.getBean(TestServiceInterface.class))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("ApplicationContext has not been initialized yet");
        }
    }

    // ==================== getBeanFromName(String, Class) ====================

    @Nested
    @DisplayName("getBeanFromName(String, Class) tests")
    class GetBeanFromNameTests {

        @Test
        @DisplayName("Should retrieve bean by name and type")
        void shouldRetrieveBeanByNameAndType() {
            TestServiceInterface service = FactoryUtil.getBeanFromName("testService", TestServiceInterface.class);

            assertThat(service).isNotNull();
            assertThat(service.execute()).isEqualTo("Executed: test");
        }

        @Test
        @DisplayName("Should throw ApiException when bean name is null")
        void shouldThrowWhenBeanNameIsNull() {
            assertThatThrownBy(() -> FactoryUtil.getBeanFromName(null, TestServiceInterface.class))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Bean name cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw ApiException when bean name is empty")
        void shouldThrowWhenBeanNameIsEmpty() {
            assertThatThrownBy(() -> FactoryUtil.getBeanFromName("", TestServiceInterface.class))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Bean name cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw ApiException when bean name is blank")
        void shouldThrowWhenBeanNameIsBlank() {
            assertThatThrownBy(() -> FactoryUtil.getBeanFromName("   ", TestServiceInterface.class))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Bean name cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw ApiException when bean class is null")
        void shouldThrowWhenBeanClassIsNull() {
            assertThatThrownBy(() -> FactoryUtil.getBeanFromName("testService", null))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Bean class cannot be null");
        }

        @Test
        @DisplayName("Should throw ApiException when named bean is not found")
        void shouldThrowWhenNamedBeanNotFound() {
            assertThatThrownBy(() -> FactoryUtil.getBeanFromName("nonExistent", TestServiceInterface.class))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Failed to retrieve bean with name 'nonExistent' and type:");
        }

        @Test
        @DisplayName("Should throw ApiException when context is not initialized")
        void shouldThrowWhenContextNotInitialized() throws Exception {
            setContextRef(null);

            assertThatThrownBy(() -> FactoryUtil.getBeanFromName("testService", TestServiceInterface.class))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("ApplicationContext has not been initialized yet");
        }
    }

    // ==================== getBeansOfType(Class) ====================

    @Nested
    @DisplayName("getBeansOfType(Class) tests")
    class GetBeansOfTypeTests {

        @Test
        @DisplayName("Should retrieve all beans of a type")
        void shouldRetrieveAllBeansOfType() {
            Map<String, TestServiceInterface> beans = FactoryUtil.getBeansOfType(TestServiceInterface.class);

            assertThat(beans)
                    .isNotEmpty()
                    .containsKey("testService");
        }

        @Test
        @DisplayName("Should return empty map when no beans of type exist")
        void shouldReturnEmptyMapWhenNoBeansOfType() {
            Map<String, Runnable> beans = FactoryUtil.getBeansOfType(Runnable.class);

            assertThat(beans).isEmpty();
        }

        @Test
        @DisplayName("Should throw ApiException when bean class is null")
        void shouldThrowWhenBeanClassIsNull() {
            assertThatThrownBy(() -> FactoryUtil.getBeansOfType(null))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Bean class cannot be null");
        }

        @Test
        @DisplayName("Should throw ApiException when context is not initialized")
        void shouldThrowWhenContextNotInitialized() throws Exception {
            setContextRef(null);

            assertThatThrownBy(() -> FactoryUtil.getBeansOfType(TestServiceInterface.class))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("ApplicationContext has not been initialized yet");
        }
    }

    // ==================== getBeanOptional(Class) ====================

    @Nested
    @DisplayName("getBeanOptional(Class) tests")
    class GetBeanOptionalTests {

        @Test
        @DisplayName("Should return Optional with bean when found")
        void shouldReturnOptionalWithBean() {
            Optional<TestServiceInterface> result = FactoryUtil.getBeanOptional(TestServiceInterface.class);

            assertThat(result).isPresent();
            assertThat(result.get().execute()).isEqualTo("Executed: test");
        }

        @Test
        @DisplayName("Should return empty Optional when bean class is null")
        void shouldReturnEmptyWhenBeanClassIsNull() {
            Optional<?> result = FactoryUtil.getBeanOptional(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty Optional when context is not initialized")
        void shouldReturnEmptyWhenContextNotInitialized() throws Exception {
            setContextRef(null);

            Optional<TestServiceInterface> result = FactoryUtil.getBeanOptional(TestServiceInterface.class);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty Optional when bean is not found")
        void shouldReturnEmptyWhenBeanNotFound() {
            Optional<Runnable> result = FactoryUtil.getBeanOptional(Runnable.class);

            assertThat(result).isEmpty();
        }
    }

    // ==================== getBeanOptional(String, Class) ====================

    @Nested
    @DisplayName("getBeanOptional(String, Class) tests")
    class GetBeanOptionalByNameTests {

        @Test
        @DisplayName("Should return Optional with named bean when found")
        void shouldReturnOptionalWithNamedBean() {
            Optional<TestServiceInterface> result = FactoryUtil.getBeanOptional("testService", TestServiceInterface.class);

            assertThat(result).isPresent();
            assertThat(result.get().execute()).isEqualTo("Executed: test");
        }

        @ParameterizedTest(name = "Should return empty Optional for bean name=''{0}''")
        @NullSource
        @ValueSource(strings = {"", "   ", "nonExistent"})
        @DisplayName("Should return empty Optional when bean name is null, blank or not found")
        void shouldReturnEmptyForInvalidOrNotFoundBeanName(String beanName) {
            Optional<TestServiceInterface> result = FactoryUtil.getBeanOptional(beanName, TestServiceInterface.class);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty Optional when bean class is null")
        void shouldReturnEmptyWhenBeanClassIsNull() {
            Optional<?> result = FactoryUtil.getBeanOptional("testService", null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty Optional when context is not initialized")
        void shouldReturnEmptyWhenContextNotInitialized() throws Exception {
            setContextRef(null);

            Optional<TestServiceInterface> result = FactoryUtil.getBeanOptional("testService", TestServiceInterface.class);

            assertThat(result).isEmpty();
        }
    }

    // ==================== isContextInitialized() ====================

    @Nested
    @DisplayName("isContextInitialized() tests")
    class IsContextInitializedTests {

        @Test
        @DisplayName("Should return true when context is initialized")
        void shouldReturnTrueWhenInitialized() {
            assertThat(FactoryUtil.isContextInitialized()).isTrue();
        }

        @Test
        @DisplayName("Should return false when context is not initialized")
        void shouldReturnFalseWhenNotInitialized() throws Exception {
            setContextRef(null);

            assertThat(FactoryUtil.isContextInitialized()).isFalse();
        }
    }

    // ==================== setApplicationContext() ====================

    @Nested
    @DisplayName("setApplicationContext() tests")
    class SetApplicationContextTests {

        @Test
        @DisplayName("Should set context on first call")
        void shouldSetContextOnFirstCall() throws Exception {
            setContextRef(null);

            FactoryUtil factoryUtil = new FactoryUtil();
            factoryUtil.setApplicationContext(applicationContext);

            assertThat(FactoryUtil.isContextInitialized()).isTrue();
        }

        @Test
        @DisplayName("Should keep original context when setting same context again")
        void shouldKeepOriginalWhenSameContext() throws Exception {
            FactoryUtil factoryUtil = new FactoryUtil();
            factoryUtil.setApplicationContext(applicationContext);

            assertThat(FactoryUtil.isContextInitialized()).isTrue();
            assertThat(getContextRef()).isSameAs(applicationContext);
        }

        @Test
        @DisplayName("Should keep original context when setting different context")
        void shouldKeepOriginalWhenDifferentContext() throws Exception {
            ApplicationContext originalContext = getContextRef();
            assertThat(originalContext).isNotNull();

            FactoryUtil factoryUtil = new FactoryUtil();
            ApplicationContext differentContext = new org.springframework.context.annotation.AnnotationConfigApplicationContext();
            factoryUtil.setApplicationContext(differentContext);

            assertThat(getContextRef()).isSameAs(originalContext);
        }
    }

    // ==================== Integration tests ====================

    @Nested
    @DisplayName("Integration tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should retrieve AnotherTestService bean")
        void shouldRetrieveAnotherTestService() {
            AnotherTestService service = FactoryUtil.getBean(AnotherTestService.class);

            assertThat(service).isNotNull();
            assertThat(service.getCounter()).isZero();
        }

        @Test
        @DisplayName("Should retrieve named AnotherTestService bean")
        void shouldRetrieveNamedAnotherTestService() {
            AnotherTestService service = FactoryUtil.getBeanFromName("anotherTestService", AnotherTestService.class);

            assertThat(service).isNotNull();
        }

        @Test
        @DisplayName("Should retrieve TestServiceImpl as concrete type")
        void shouldRetrieveConcreteType() {
            Optional<TestServiceImpl> result = FactoryUtil.getBeanOptional(TestServiceImpl.class);

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("test");
        }
    }

    // ==================== getBeansOfType exception path ====================

    @Nested
    @DisplayName("getBeansOfType() exception path tests")
    class GetBeansOfTypeExceptionTests {

        @Test
        @DisplayName("Should throw ApiException when getBeansOfType throws BeansException")
        void shouldThrowWhenGetBeansOfTypeThrowsBeansException() throws Exception {
            // Given - replace context with a mock that throws on getBeansOfType
            ApplicationContext mockContext = Mockito.mock(ApplicationContext.class);
            when(mockContext.getBeansOfType(TestServiceInterface.class))
                    .thenThrow(new org.springframework.beans.factory.BeanCreationException("test error"));

            setContextRef(mockContext);

            // When/Then
            assertThatThrownBy(() -> FactoryUtil.getBeansOfType(TestServiceInterface.class))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Failed to retrieve beans with type:");
        }
    }
}
