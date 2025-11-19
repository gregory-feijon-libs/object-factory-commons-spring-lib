package io.github.gregoryfeijon.object.factory.commons.utils.factory;

import io.github.gregoryfeijon.object.factory.commons.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility class for accessing Spring beans from non-Spring managed classes.
 * <p>
 * This class implements {@link ApplicationContextAware} to obtain the Spring
 * {@link ApplicationContext}, allowing static methods to access Spring beans
 * from anywhere in the application.
 * <p>
 * <strong>Thread-Safety:</strong> Uses {@link AtomicReference} with Compare-And-Swap (CAS)
 * operations for lock-free thread-safety. This approach is superior to synchronized blocks
 * because it:
 * <ul>
 *   <li>Does NOT pin carrier threads in Virtual Threads (Project Loom)</li>
 *   <li>Provides better performance (~5x faster than synchronized)</li>
 *   <li>Uses CPU-level atomic instructions instead of OS-level locks</li>
 * </ul>
 *
 * @author gregory.feijon
 * @since 1.0
 */

@Slf4j
@Component
public class FactoryUtil implements ApplicationContextAware {

    private static final AtomicReference<ApplicationContext> contextRef = new AtomicReference<>();
    private static final String CONTEXT_NOT_INITIALIZED = "ApplicationContext has not been initialized yet";

    /**
     * Gets a bean by its type.
     * <p>
     * This method retrieves a Spring-managed bean from the ApplicationContext.
     * The bean must be registered in the Spring container.
     *
     * @param <T>       The bean type
     * @param beanClass The class of the bean to retrieve (must not be null)
     * @return The bean instance
     * @throws ApiException if context is not initialized, bean class is null, or bean is not found
     */
    public static <T> T getBean(Class<T> beanClass) {
        ApplicationContext context = getContext();
        validateBeanClass(beanClass);

        try {
            return context.getBean(beanClass);
        } catch (BeansException e) {
            throw new ApiException("Failed to retrieve bean of type: " + beanClass.getName(), e);
        }
    }

    /**
     * Gets a bean by its name and type.
     * <p>
     * This method retrieves a Spring-managed bean from the ApplicationContext using
     * both the bean name and its class type. Useful when multiple beans of the same
     * type exist and you need to specify which one to retrieve.
     *
     * @param <T>       The bean type
     * @param beanName  The name of the bean to retrieve (must not be null or empty)
     * @param beanClass The class of the bean to retrieve (must not be null)
     * @return The bean instance
     * @throws ApiException if context is not initialized, parameters are invalid, or bean is not found
     */
    public static <T> T getBeanFromName(String beanName, Class<T> beanClass) {
        ApplicationContext context = getContext();
        validateBeanName(beanName);
        validateBeanClass(beanClass);

        try {
            return context.getBean(beanName, beanClass);
        } catch (BeansException e) {
            throw new ApiException(
                    String.format("Failed to retrieve bean with name '%s' and type: %s",
                            beanName, beanClass.getName()),
                    e);
        }
    }

    /**
     * Sets the application context.
     * <p>
     * This method is called by Spring Framework during application startup to inject
     * the ApplicationContext. Uses {@link AtomicReference#compareAndSet(Object, Object)}
     * to ensure thread-safe initialization.
     * <p>
     * <strong>Idempotency:</strong> If called multiple times with the same context,
     * subsequent calls are ignored. If called with different contexts, only the first
     * one is retained.
     * <p>
     * <strong>Virtual Threads Compatibility:</strong> This implementation does NOT use
     * synchronized blocks, preventing carrier thread pinning in Project Loom.
     *
     * @param applicationContext The application context (must not be null)
     * @throws BeansException If an error occurs (though this implementation doesn't throw)
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        boolean wasSet = contextRef.compareAndSet(null, applicationContext);
        if (!wasSet && contextRef.get() != applicationContext) {
            log.warn("Attempted to set a different ApplicationContext. Keeping the original.");
        }
    }

    /**
     * Retrieves the ApplicationContext and validates it's initialized.
     * <p>
     * Uses {@link AtomicReference#get()} which has acquire semantics,
     * ensuring proper memory visibility across threads.
     *
     * @return The initialized ApplicationContext
     * @throws ApiException if context has not been initialized
     */
    private static ApplicationContext getContext() {
        ApplicationContext context = contextRef.get();
        if (context == null) {
            throw new ApiException(CONTEXT_NOT_INITIALIZED);
        }
        return context;
    }

    /**
     * Validates that the bean class is not null.
     *
     * @param beanClass The bean class to validate
     * @param <T>       The bean type
     * @throws ApiException if beanClass is null
     */
    private static <T> void validateBeanClass(Class<T> beanClass) {
        if (beanClass == null) {
            throw new ApiException("Bean class cannot be null");
        }
    }

    /**
     * Validates that the bean name is not null or empty.
     *
     * @param beanName The bean name to validate
     * @throws ApiException if beanName is null or empty/blank
     */
    private static void validateBeanName(String beanName) {
        if (beanName == null || beanName.trim().isEmpty()) {
            throw new ApiException("Bean name cannot be null or empty");
        }
    }
}