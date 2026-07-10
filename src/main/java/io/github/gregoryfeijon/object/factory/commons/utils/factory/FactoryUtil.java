package io.github.gregoryfeijon.object.factory.commons.utils.factory;

import io.github.gregoryfeijon.object.factory.commons.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
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
     * Retrieves all beans of the specified type.
     * <p>
     * This method returns a {@link Map} containing all Spring-managed beans that
     * match the provided type. The map keys are the bean names and the values are
     * the corresponding bean instances.
     * <p>
     * Useful when multiple beans implement the same interface or extend the same
     * superclass and you want to process all of them.
     * <p>
     * <strong>Thread-Safety:</strong> The underlying {@link ApplicationContext} is
     * safely obtained through the CAS-protected reference, ensuring visibility and
     * consistency without synchronized blocks.
     *
     * @param <T>       The generic type of the beans to retrieve
     * @param beanClass The class type of the beans to fetch (must not be null)
     * @return A map where the key is the bean name and the value is the bean instance
     * @throws ApiException if context is not initialized, beanClass is null,
     *                      or bean retrieval fails
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> beanClass) {
        ApplicationContext context = getContext();
        validateBeanClass(beanClass);

        try {
            return context.getBeansOfType(beanClass);
        } catch (BeansException e) {
            throw new ApiException(
                    String.format("Failed to retrieve beans with type: %s", beanClass.getName()), e);
        }
    }

    // ==================== Optional Bean Retrieval ====================

    /**
     * Gets a bean by its type, returning an Optional instead of throwing an exception.
     * <p>
     * This method provides a safer way to retrieve beans when the bean might not exist.
     * Instead of throwing an exception, it returns an empty Optional.
     * <p>
     * <strong>Example:</strong>
     * <pre>
     * Optional&lt;MyService&gt; service = FactoryUtil.getBeanOptional(MyService.class);
     * service.ifPresent(s -&gt; s.doSomething());
     * </pre>
     *
     * @param <T>       The bean type
     * @param beanClass The class of the bean to retrieve (must not be null)
     * @return An Optional containing the bean, or empty if not found or context not initialized
     */
    public static <T> Optional<T> getBeanOptional(Class<T> beanClass) {
        if (beanClass == null) {
            return Optional.empty();
        }

        ApplicationContext context = contextRef.get();
        if (context == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(context.getBean(beanClass));
        } catch (BeansException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets a bean by its name and type, returning an Optional instead of throwing an exception.
     * <p>
     * This method provides a safer way to retrieve beans when the bean might not exist.
     * <p>
     * <strong>Example:</strong>
     * <pre>
     * Optional&lt;MyService&gt; service = FactoryUtil.getBeanOptional("myService", MyService.class);
     * MyService s = service.orElseGet(DefaultService::new);
     * </pre>
     *
     * @param <T>       The bean type
     * @param beanName  The name of the bean to retrieve
     * @param beanClass The class of the bean to retrieve
     * @return An Optional containing the bean, or empty if not found or context not initialized
     */
    public static <T> Optional<T> getBeanOptional(String beanName, Class<T> beanClass) {
        if (isInvalidBeanNameOrClass(beanName, beanClass)) {
            return Optional.empty();
        }

        ApplicationContext context = contextRef.get();
        if (context == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(context.getBean(beanName, beanClass));
        } catch (BeansException e) {
            return Optional.empty();
        }
    }

    /**
     * Checks whether a bean name/class pair is invalid for lookup.
     *
     * @param beanName  The bean name to check
     * @param beanClass The bean class to check
     * @return true if the bean name is null/blank or the bean class is null
     */
    private static <T> boolean isInvalidBeanNameOrClass(String beanName, Class<T> beanClass) {
        return beanName == null || beanName.trim().isEmpty() || beanClass == null;
    }

    /**
     * Checks if the ApplicationContext has been initialized.
     * <p>
     * Useful for conditional logic that depends on Spring context availability.
     *
     * @return true if the context is initialized, false otherwise
     */
    public static boolean isContextInitialized() {
        return contextRef.get() != null;
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