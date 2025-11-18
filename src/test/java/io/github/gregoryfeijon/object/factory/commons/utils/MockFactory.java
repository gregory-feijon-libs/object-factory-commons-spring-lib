package io.github.gregoryfeijon.object.factory.commons.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mock factory for creating commonly used mocks in tests
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MockFactory {

    /**
     * Creates a mock ApplicationContext with common configurations
     */
    public static ApplicationContext createMockApplicationContext() {
        ApplicationContext mockContext = mock(ApplicationContext.class);

        // Setup common behavior if needed
        when(mockContext.getApplicationName()).thenReturn("TestApplication");

        return mockContext;
    }

    /**
     * Creates a mock ApplicationContext that throws exceptions for undefined beans
     */
    public static ApplicationContext createStrictMockApplicationContext() {
        ApplicationContext mockContext = mock(ApplicationContext.class);

        // By default, throw exception for any bean request
        when(mockContext.getBean(any(Class.class)))
                .thenThrow(new NoSuchBeanDefinitionException("Bean not defined in test"));

        when(mockContext.getBean(anyString(), any(Class.class)))
                .thenThrow(new NoSuchBeanDefinitionException("Bean not defined in test"));

        return mockContext;
    }

    /**
     * Configures a mock ApplicationContext to return a specific bean
     */
    public static <T> void configureBeanInContext(
            ApplicationContext mockContext,
            Class<T> beanClass,
            T beanInstance) {

        when(mockContext.getBean(beanClass)).thenReturn(beanInstance);
    }

    /**
     * Configures a mock ApplicationContext to return a named bean
     */
    public static <T> void configureNamedBeanInContext(
            ApplicationContext mockContext,
            String beanName,
            Class<T> beanClass,
            T beanInstance) {

        when(mockContext.getBean(beanName, beanClass)).thenReturn(beanInstance);
    }
}