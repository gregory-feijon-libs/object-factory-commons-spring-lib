package io.github.gregoryfeijon.object.factory.commons.utils.factory;

import lombok.NoArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Utility class for accessing Spring beans from non-Spring managed classes.
 * <p>
 * This class implements {@link ApplicationContextAware} to obtain the Spring
 * {@link ApplicationContext}, allowing static methods to access Spring beans
 * from anywhere in the application.
 *
 * @author gregory.feijon
 */
@Component
@NoArgsConstructor
public class FactoryUtil implements ApplicationContextAware {
    private static ApplicationContext context;
    private static final Object LOCK = new Object();

    /**
     * Gets a bean by its type.
     *
     * @param <T>       The bean type
     * @param beanClass The class of the bean to retrieve
     * @return The bean instance
     */
    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    /**
     * Gets a bean by its name and type.
     *
     * @param <T>       The bean type
     * @param beanName  The name of the bean to retrieve
     * @param beanClass The class of the bean to retrieve
     * @return The bean instance
     */
    public static <T> T getBeanFromName(String beanName, Class<T> beanClass) {
        return context.getBean(beanName, beanClass);
    }

    /**
     * Sets the application context.
     * <p>
     * This method is called by Spring to provide the application context.
     * Thread-safe implementation to ensure the context is set only once.
     *
     * @param applicationContext The application context
     * @throws BeansException If an error occurs while setting the context
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        synchronized (LOCK) {
            if (context == null) {
                context = applicationContext;
            }
        }
    }
}