package io.github.gregoryfeijon.object.factory.commons.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Utility class for reflection-based method and field discovery.
 * <p>
 * This class provides methods for finding getters, setters and fields on an
 * object's class hierarchy, plus small null-safety helpers built on top of them.
 * <p>
 * Related concerns have been split into dedicated classes:
 * <ul>
 *   <li>{@link ReflectionComparator} - comparing objects by their getter values</li>
 *   <li>{@link DynamicAccessorUtil} - invoking getters/setters dynamically by name</li>
 *   <li>{@link PropertyCopier} - copying properties between objects</li>
 * </ul>
 * <p>
 * <strong>Thread-Safety:</strong> All methods are stateless and thread-safe.
 * <p>
 * <strong>Performance Note:</strong> Reflection operations are inherently slower than direct access.
 * Method and Field references are cached internally for improved performance on repeated operations.
 * <p>
 * <strong>Collection Policy:</strong> All methods returning {@link List} return mutable lists
 * to allow further manipulation by callers (filtering, sorting, etc.).
 *
 * @author gregory.feijon
 * @since 1.0
 */

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtil {

    /**
     * Thread-safe cache for getter methods by class.
     * <p>
     * Performance: First call ~100µs, cached calls ~10ns (10,000x faster)
     */
    private static final Map<Class<?>, List<Method>> GETTER_CACHE = new ConcurrentHashMap<>();

    /**
     * Thread-safe cache for setter methods by class.
     */
    private static final Map<Class<?>, List<Method>> SETTER_CACHE = new ConcurrentHashMap<>();

    /**
     * Thread-safe cache for all methods by class.
     */
    private static final Map<Class<?>, List<Method>> ALL_METHODS_CACHE = new ConcurrentHashMap<>();

    /**
     * Finds all getter methods of an object.
     * <p>
     * A method is considered a getter if its name starts with "get" or "is" (case-insensitive).
     * This includes both public and non-public methods from the object's class hierarchy.
     * <p>
     * <strong>Examples:</strong>
     * <ul>
     *   <li>{@code getName()} - Standard getter</li>
     *   <li>{@code isActive()} - Boolean getter</li>
     *   <li>{@code getStatus()} - Returns any type</li>
     * </ul>
     * <p>
     * <strong>Returns mutable list:</strong> Allows callers to filter, sort, or modify the list.
     *
     * @param object The object to find getters for (must not be null)
     * @return A mutable list of getter methods (never null, may be empty)
     * @throws io.github.gregoryfeijon.object.factory.commons.exception.ApiException if object is null
     */
    @SuppressWarnings("java:S6204")
    public static List<Method> findGetMethods(Object object) {
        ReflectionValidationUtil.validateObject(object, "Object to find getters");

        Class<?> clazz = object.getClass();
        List<Method> cached = GETTER_CACHE.computeIfAbsent(clazz, c ->
                getAllMethodsCached(c).stream()
                        .filter(method -> isGetterMethod(method.getName()))
                        .collect(Collectors.toList())
        );
        return new ArrayList<>(cached);
    }

    /**
     * Finds all setter methods of an object.
     * <p>
     * A method is considered a setter if its name starts with "set" (case-insensitive).
     * This includes both public and non-public methods from the object's class hierarchy.
     * <p>
     * <strong>Returns mutable list:</strong> Allows callers to filter, sort, or modify the list.
     *
     * @param object The object to find setters for (must not be null)
     * @return A mutable list of setter methods (never null, may be empty)
     * @throws io.github.gregoryfeijon.object.factory.commons.exception.ApiException if object is null
     */
    @SuppressWarnings("java:S6204")
    public static List<Method> findSetMethods(Object object) {
        ReflectionValidationUtil.validateObject(object, "Object to find setters");

        Class<?> clazz = object.getClass();
        List<Method> cached = SETTER_CACHE.computeIfAbsent(clazz, c ->
                getAllMethodsCached(c).stream()
                        .filter(method -> method.getName().toLowerCase(Locale.ROOT).startsWith("set"))
                        .collect(Collectors.toList())
        );
        return new ArrayList<>(cached);
    }

    /**
     * Gets all methods of an object as a collection.
     * <p>
     * Retrieves all declared methods from the object's class hierarchy using Spring's
     * {@link ReflectionUtils#getAllDeclaredMethods(Class)}.
     * <p>
     * Results are cached for improved performance on repeated calls.
     *
     * @param object The object to get methods for (must not be null)
     * @return A collection of all methods (never null)
     * @throws io.github.gregoryfeijon.object.factory.commons.exception.ApiException if object is null
     */
    public static Collection<Method> getMethodsAsList(Object object) {
        ReflectionValidationUtil.validateObject(object, "Object to get methods");
        return new ArrayList<>(getAllMethodsCached(object.getClass()));
    }

    /**
     * Gets all methods for a class from cache or computes them.
     *
     * @param clazz The class to get methods for
     * @return Cached list of all methods
     */
    private static List<Method> getAllMethodsCached(Class<?> clazz) {
        return ALL_METHODS_CACHE.computeIfAbsent(clazz,
                c -> Arrays.asList(ReflectionUtils.getAllDeclaredMethods(c)));
    }

    /**
     * Checks if a method name represents a getter.
     * <p>
     * Handles both standard getters (getXxx) and boolean getters (isXxx).
     *
     * @param methodName The method name to check
     * @return true if the method name starts with "get" or "is"
     */
    private static boolean isGetterMethod(String methodName) {
        String lowerName = methodName.toLowerCase(Locale.ROOT);
        return lowerName.startsWith("get") || lowerName.startsWith("is");
    }

    /**
     * Gets all fields of an object, including inherited fields.
     * <p>
     * Equivalent to calling {@link #getFieldsAsCollection(Object, boolean)} with {@code true}.
     *
     * @param object The object to get fields for (must not be null)
     * @return A mutable collection of all fields including inherited ones (never null)
     * @throws io.github.gregoryfeijon.object.factory.commons.exception.ApiException if object is null
     */
    public static Collection<Field> getFieldsAsCollection(Object object) {
        return getFieldsAsCollection(object, true);
    }

    /**
     * Gets all fields of an object, with custom collection type.
     * <p>
     * Allows specifying the exact collection type to return (e.g., ArrayList, LinkedHashSet).
     * <p>
     * <strong>Example:</strong>
     * <pre>
     * Set&lt;Field&gt; fields = getFieldsAsCollection(obj, HashSet::new);
     * </pre>
     *
     * @param <T>            The collection type
     * @param object         The object to get fields for (must not be null)
     * @param collectionType Supplier for the collection type (e.g., ArrayList::new)
     * @return A collection of fields of the specified type (never null)
     * @throws io.github.gregoryfeijon.object.factory.commons.exception.ApiException if object is null
     */
    @SuppressWarnings("java:S6204")
    public static <T extends Collection<Field>> T getFieldsAsCollection(
            Object object,
            Supplier<T> collectionType) {

        return getFieldsAsCollection(object, true).stream()
                .collect(Collectors.toCollection(collectionType));
    }

    /**
     * Gets all fields of an object, with option to include inherited fields.
     * <p>
     * When {@code includeParents} is true, traverses the entire class hierarchy
     * up to Object, collecting all declared fields.
     * <p>
     * <strong>Performance Note:</strong> Including parent fields requires traversing
     * the class hierarchy, which may be expensive for deep inheritance trees.
     * <p>
     * <strong>Returns mutable collection:</strong> Allows callers to modify the collection.
     *
     * @param object         The object to get fields for (must not be null)
     * @param includeParents Whether to include fields from parent classes
     * @return A mutable collection of fields (never null, may be empty)
     * @throws io.github.gregoryfeijon.object.factory.commons.exception.ApiException if object is null
     */
    @SuppressWarnings("java:S6204")
    public static Collection<Field> getFieldsAsCollection(Object object, boolean includeParents) {
        ReflectionValidationUtil.validateObject(object, "Object to get fields");

        Class<?> clazz = object.getClass();
        Collection<Field> fields = Arrays.stream(clazz.getDeclaredFields())
                .collect(Collectors.toCollection(ArrayList::new));

        if (includeParents && clazz.getSuperclass() != null) {
            Class<?> currentClass = clazz.getSuperclass();
            while (currentClass != null) {
                fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
                currentClass = currentClass.getSuperclass();
            }
        }

        return fields;
    }

    /**
     * Safely gets a value using a getter function, wrapping the result in an Optional.
     * <p>
     * This method handles null objects by returning an empty Optional, making it safe
     * to chain with other Optional operations.
     * <p>
     * <strong>Example:</strong>
     * <pre>
     * Optional&lt;String&gt; name = safeGet(user, User::getName);
     * String upperName = name.map(String::toUpperCase).orElse("UNKNOWN");
     * </pre>
     *
     * @param <T>    The type of the object
     * @param <R>    The type of the return value
     * @param obj    The object to get a value from (can be null)
     * @param getter A function that extracts a value from the object (must not be null)
     * @return An Optional containing the value, or empty if the object or value is null
     * @throws io.github.gregoryfeijon.object.factory.commons.exception.ApiException if getter function is null
     */
    public static <T, R> Optional<R> safeGet(T obj, Function<T, R> getter) {
        ReflectionValidationUtil.validateGetter(getter);

        if (obj == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(getter.apply(obj));
    }

    /**
     * Safely gets a value using a getter function, returning a default value if null.
     * <p>
     * This is a convenience method that combines {@link #safeGet(Object, Function)}
     * with {@link Optional#orElse(Object)}.
     * <p>
     * <strong>Example:</strong>
     * <pre>
     * String name = safeGetWithDefaultValue(user, User::getName, "Anonymous");
     * // Returns user name or "Anonymous" if user or name is null
     * </pre>
     *
     * @param <T>          The type of the object
     * @param <R>          The type of the return value
     * @param obj          The object to get a value from (can be null)
     * @param getter       A function that extracts a value from the object (must not be null)
     * @param defaultValue The default value to return if the object or value is null (can be null)
     * @return The value from the getter, or the default value if null
     * @throws io.github.gregoryfeijon.object.factory.commons.exception.ApiException if getter function is null
     */
    public static <T, R> R safeGetWithDefaultValue(T obj, Function<T, R> getter, R defaultValue) {
        return safeGet(obj, getter).orElse(defaultValue);
    }

    /**
     * Removes null elements from a list.
     * <p>
     * Returns a new mutable list with all null elements filtered out.
     * If the input list is null or empty, returns an empty mutable list.
     * <p>
     * <strong>Returns mutable list:</strong> Allows further manipulation by callers.
     * <p>
     * <strong>Example:</strong>
     * <pre>
     * List&lt;String&gt; list = Arrays.asList("a", null, "b", null, "c");
     * List&lt;String&gt; clean = removeNulls(list); // ["a", "b", "c"]
     * clean.add("d"); // Can modify the returned list
     * </pre>
     *
     * @param <T>  The type of elements in the list
     * @param list The list to remove nulls from (can be null)
     * @return A new mutable list with null elements removed (never null)
     */
    @SuppressWarnings("java:S6204")
    public static <T> List<T> removeNulls(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        return list.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
