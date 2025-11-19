package io.github.gregoryfeijon.object.factory.commons.utils;

import io.github.gregoryfeijon.object.factory.commons.exception.ApiException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Utility class for reflection operations.
 * <p>
 * This class provides methods for finding and invoking getters and setters,
 * working with fields, and performing other reflection-based operations.
 *
 * @author gregory.feijon
 */

/**
 * Utility class for reflection operations.
 * <p>
 * This class provides methods for finding and invoking getters and setters,
 * working with fields, comparing objects, and performing other reflection-based operations.
 * <p>
 * <strong>Thread-Safety:</strong> All methods are stateless and thread-safe.
 * <p>
 * <strong>Performance Note:</strong> Reflection operations are inherently slower than direct access.
 * Consider caching Method/Field references when performing repeated operations on the same types.
 * <p>
 * <strong>Collection Policy:</strong> All methods returning {@link List} return mutable lists
 * to allow further manipulation by callers (filtering, sorting, etc.).
 *
 * @author gregory.feijon
 * @since 1.0
 */

@SuppressWarnings("java:S6204")
//warning do .toList() suprimida, uma vez que não se aplica nessa classe, que é uma classe útil
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtil {

    private static final String NULL_ENTITY_ERROR = "Entities to compare cannot be null";
    private static final String DIFFERENT_TYPES_ERROR = "Entities must be of the same type";

    // ==================== Method Discovery ====================

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
     * @throws ApiException if object is null
     */
    public static List<Method> findGetMethods(Object object) {
        validateObject(object, "Object to find getters");

        return getMethodsAsList(object).stream()
                .filter(method -> isGetterMethod(method.getName()))
                .collect(Collectors.toCollection(ArrayList::new));
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
     * @throws ApiException if object is null
     */
    public static List<Method> findSetMethods(Object object) {
        validateObject(object, "Object to find setters");

        return getMethodsAsList(object).stream()
                .filter(method -> method.getName().toLowerCase().startsWith("set"))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Gets all methods of an object as a collection.
     * <p>
     * Retrieves all declared methods from the object's class hierarchy using Spring's
     * {@link ReflectionUtils#getAllDeclaredMethods(Class)}.
     *
     * @param object The object to get methods for (must not be null)
     * @return A collection of all methods (never null)
     * @throws ApiException if object is null
     */
    public static Collection<Method> getMethodsAsList(Object object) {
        validateObject(object, "Object to get methods");
        return Arrays.asList(ReflectionUtils.getAllDeclaredMethods(object.getClass()));
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
        String lowerName = methodName.toLowerCase();
        return lowerName.startsWith("get") || lowerName.startsWith("is");
    }

    // ==================== Field Discovery ====================

    /**
     * Gets all fields of an object, including inherited fields.
     * <p>
     * Equivalent to calling {@link #getFieldsAsCollection(Object, boolean)} with {@code true}.
     *
     * @param object The object to get fields for (must not be null)
     * @return A mutable collection of all fields including inherited ones (never null)
     * @throws ApiException if object is null
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
     * @throws ApiException if object is null
     */
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
     * @throws ApiException if object is null
     */
    public static Collection<Field> getFieldsAsCollection(Object object, boolean includeParents) {
        validateObject(object, "Object to get fields");

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

    // ==================== Object Comparison ====================

    /**
     * Compares two objects of the same type by comparing all their getter values.
     * <p>
     * Returns {@code true} only if ALL getter values are equal. Uses null-safe comparison
     * with special handling for:
     * <ul>
     *   <li>Strings: null equals empty string</li>
     *   <li>Numbers: null equals zero</li>
     *   <li>Collections: null equals empty collection</li>
     * </ul>
     * <p>
     * <strong>Important:</strong> Objects must be of the same type, otherwise comparison
     * may yield unexpected results.
     *
     * @param <T>     The type of objects to compare
     * @param entity1 First object to compare (must not be null)
     * @param entity2 Second object to compare (must not be null)
     * @return true if all getter values are equal, false otherwise
     * @throws InvocationTargetException If error occurs invoking getter methods
     * @throws IllegalAccessException    If error occurs accessing getter methods
     * @throws ApiException              If entities are null or of different types
     */
    public static <T> boolean compareObjectsValues(T entity1, T entity2)
            throws InvocationTargetException, IllegalAccessException {

        validateComparisonParameters(entity1, entity2);

        List<Method> getsEntity1 = findGetMethods(entity1);
        List<Method> getsEntity2 = findGetMethods(entity2);

        return compareLists(getsEntity1, getsEntity2, entity1, entity2);
    }

    /**
     * Compares two objects of the same type, excluding specified fields from comparison.
     * <p>
     * Useful when you want to compare objects but ignore certain fields (e.g., timestamps,
     * auto-generated IDs).
     * <p>
     * <strong>Example:</strong>
     * <pre>
     * User user1 = new User("John", 25, "2024-01-01");
     * User user2 = new User("John", 25, "2024-01-02");
     * // Compare ignoring createdDate
     * boolean equal = compareObjectsValues(user1, user2, new String[]{"createdDate"});
     * // Returns true
     * </pre>
     *
     * @param <T>         The type of objects to compare
     * @param entity1     First object to compare (must not be null)
     * @param entity2     Second object to compare (must not be null)
     * @param filterNames Array of field names to exclude from comparison (can be null)
     * @return true if all non-filtered getter values are equal, false otherwise
     * @throws InvocationTargetException If error occurs invoking getter methods
     * @throws IllegalAccessException    If error occurs accessing getter methods
     * @throws ApiException              If entities are null or of different types
     */
    public static <T> boolean compareObjectsValues(T entity1, T entity2, String[] filterNames)
            throws InvocationTargetException, IllegalAccessException {

        if (filterNames == null) {
            return compareObjectsValues(entity1, entity2);
        }
        return compare(entity1, entity2, filterNames, true);
    }

    /**
     * Compares two objects of the same type, with option to exclude or include only specified fields.
     * <p>
     * This is the most flexible comparison method, allowing you to either:
     * <ul>
     *   <li>{@code remove=true}: Compare all fields EXCEPT those in filterNames</li>
     *   <li>{@code remove=false}: Compare ONLY fields in filterNames</li>
     * </ul>
     * <p>
     * <strong>Example - Exclude mode:</strong>
     * <pre>
     * compareObjectsValues(obj1, obj2, new String[]{"id", "timestamp"}, true);
     * // Compares all fields except id and timestamp
     * </pre>
     * <p>
     * <strong>Example - Include mode:</strong>
     * <pre>
     * compareObjectsValues(obj1, obj2, new String[]{"name", "email"}, false);
     * // Compares ONLY name and email fields
     * </pre>
     *
     * @param <T>         The type of objects to compare
     * @param entity1     First object to compare (must not be null)
     * @param entity2     Second object to compare (must not be null)
     * @param filterNames Array of field names to filter (must not be null)
     * @param remove      true to exclude fields, false to include only these fields
     * @return true if filtered comparison is equal, false otherwise
     * @throws InvocationTargetException If error occurs invoking getter methods
     * @throws IllegalAccessException    If error occurs accessing getter methods
     * @throws ApiException              If entities are null or of different types
     */
    public static <T> boolean compareObjectsValues(T entity1, T entity2, String[] filterNames, boolean remove)
            throws InvocationTargetException, IllegalAccessException {

        return compare(entity1, entity2, filterNames, remove);
    }

    /**
     * Internal method to perform filtered comparison.
     * <p>
     * Retrieves getters, filters them based on parameters, and compares values.
     *
     * @param <T>         The type of objects to compare
     * @param entity1     First object to compare
     * @param entity2     Second object to compare
     * @param filterNames Field names to filter
     * @param remove      true to exclude fields, false to include only
     * @return true if comparison is equal, false otherwise
     * @throws InvocationTargetException If error occurs invoking getter methods
     * @throws IllegalAccessException    If error occurs accessing getter methods
     */
    private static <T> boolean compare(T entity1, T entity2, String[] filterNames, boolean remove)
            throws InvocationTargetException, IllegalAccessException {

        validateComparisonParameters(entity1, entity2);

        List<Method> getsEntity1 = findGetMethods(entity1);
        List<Method> getsEntity2 = findGetMethods(entity2);

        getsEntity1 = filterList(getsEntity1, filterNames, remove);
        getsEntity2 = filterList(getsEntity2, filterNames, remove);

        return compareLists(getsEntity1, getsEntity2, entity1, entity2);
    }

    /**
     * Compares two lists of getter methods by invoking them and comparing values.
     * <p>
     * For each getter in list1, finds the corresponding getter in list2 (by name),
     * invokes both, and compares the returned values using null-safe comparison.
     * <p>
     * <strong>Special Comparison Rules:</strong>
     * <ul>
     *   <li>Uses {@link ObjectUtils#nullSafeEquals(Object, Object)} as base comparison</li>
     *   <li>Strings: null equals empty string ("")</li>
     *   <li>Numbers: null equals zero (0, 0L, 0.0, etc.)</li>
     *   <li>Collections: null equals empty collection</li>
     * </ul>
     *
     * @param getsEntity1 Getter methods from first entity
     * @param getsEntity2 Getter methods from second entity
     * @param entity1     First entity instance
     * @param entity2     Second entity instance
     * @return true if all getter values are equal, false otherwise
     * @throws InvocationTargetException If error occurs invoking getter methods
     * @throws IllegalAccessException    If error occurs accessing getter methods
     */
    private static boolean compareLists(
            List<Method> getsEntity1,
            List<Method> getsEntity2,
            Object entity1,
            Object entity2) throws InvocationTargetException, IllegalAccessException {

        for (Method methodEntity1 : getsEntity1) {
            Optional<Method> methodEntity2 = getsEntity2.stream()
                    .filter(method -> method.getName().equalsIgnoreCase(methodEntity1.getName()))
                    .findAny();

            if (methodEntity2.isPresent()) {
                Object value1 = methodEntity1.invoke(entity1);
                Object value2 = methodEntity2.get().invoke(entity2);

                if (!ObjectUtils.nullSafeEquals(value1, value2)) {
                    if (!areReturnTypesEqual(methodEntity1, methodEntity2.get())) {
                        return false;
                    }

                    if (!areValuesEquivalent(value1, value2, methodEntity1.getReturnType())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Checks if two methods have the same return type.
     *
     * @param method1 First method
     * @param method2 Second method
     * @return true if return types are equal
     */
    private static boolean areReturnTypesEqual(Method method1, Method method2) {
        return method1.getReturnType() == method2.getReturnType();
    }

    /**
     * Determines if two values should be considered equivalent based on type-specific rules.
     * <p>
     * This method implements special comparison logic for common types where null
     * should be treated as equal to certain "empty" or "zero" values.
     *
     * @param value1 First value
     * @param value2 Second value
     * @param type   The type of the values
     * @return true if values are equivalent by type-specific rules
     */
    private static boolean areValuesEquivalent(Object value1, Object value2, Class<?> type) {
        if (type.isAssignableFrom(String.class)) {
            return areStringsEquivalent(value1, value2);
        }
        if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(Double.class)) {
            return areNumbersEquivalent(value1, value2);
        }
        if (type.isAssignableFrom(Collection.class)) {
            return areCollectionsEquivalent(value1, value2);
        }
        return false;
    }

    /**
     * Checks if strings are equivalent (null equals empty string).
     * <p>
     * <strong>Equivalence Rules:</strong>
     * <ul>
     *   <li>null == "" → true</li>
     *   <li>"" == null → true</li>
     *   <li>null == null → true (handled by nullSafeEquals)</li>
     *   <li>"" == "" → true (handled by nullSafeEquals)</li>
     * </ul>
     *
     * @param value1 First string value
     * @param value2 Second string value
     * @return true if strings are equivalent
     */
    private static boolean areStringsEquivalent(Object value1, Object value2) {
        if (value1 == null && value2 != null) {
            return isStringEmpty(value2);
        }
        if (value2 == null && value1 != null) {
            return isStringEmpty(value1);
        }
        return false;
    }

    /**
     * Checks if a string value is empty.
     *
     * @param value The string value to check
     * @return true if the string is empty
     */
    private static boolean isStringEmpty(Object value) {
        return ((String) value).isEmpty();
    }

    /**
     * Checks if numbers are equivalent (null equals zero).
     * <p>
     * <strong>Equivalence Rules:</strong>
     * <ul>
     *   <li>null == 0 → true</li>
     *   <li>null == 0L → true</li>
     *   <li>null == 0.0 → true</li>
     *   <li>0 == null → true</li>
     * </ul>
     *
     * @param value1 First number value
     * @param value2 Second number value
     * @return true if numbers are equivalent
     */
    private static boolean areNumbersEquivalent(Object value1, Object value2) {
        if (value1 == null && value2 != null) {
            return isNumberZero(value2);
        }
        if (value2 == null && value1 != null) {
            return isNumberZero(value1);
        }
        return false;
    }

    /**
     * Checks if a number is zero using BigDecimal comparison.
     * <p>
     * Converts the number to BigDecimal for accurate zero comparison,
     * handling all numeric types uniformly.
     *
     * @param value The number value to check
     * @return true if the number is zero
     */
    private static boolean isNumberZero(Object value) {
        BigDecimal decimal = BigDecimal.valueOf(((Number) value).doubleValue());
        return decimal.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Checks if collections are equivalent (null equals empty collection).
     * <p>
     * <strong>Equivalence Rules:</strong>
     * <ul>
     *   <li>null == empty collection → true</li>
     *   <li>empty collection == null → true</li>
     * </ul>
     *
     * @param value1 First collection value
     * @param value2 Second collection value
     * @return true if collections are equivalent
     */
    private static boolean areCollectionsEquivalent(Object value1, Object value2) {
        if (value1 == null && value2 != null) {
            return isCollectionEmpty(value2);
        }
        if (value2 == null && value1 != null) {
            return isCollectionEmpty(value1);
        }
        return false;
    }

    /**
     * Checks if a collection is empty.
     *
     * @param value The collection value to check
     * @return true if the collection is empty
     */
    private static boolean isCollectionEmpty(Object value) {
        return CollectionUtils.isEmpty((Collection<?>) value);
    }

    // ==================== Method Filtering ====================

    /**
     * Filters a list of methods based on field names.
     * <p>
     * This method constructs getter/setter names from field names and filters
     * the method list accordingly. For each field name, it looks for:
     * <ul>
     *   <li>getFieldName() or isFieldName() for getters</li>
     *   <li>setFieldName() for setters</li>
     * </ul>
     * <p>
     * <strong>Case Insensitive:</strong> Matching is case-insensitive.
     * <p>
     * <strong>Modifies list in-place:</strong> When {@code remove=true}, the input list
     * is modified. When {@code remove=false}, a new list is returned.
     * <p>
     * <strong>Examples:</strong>
     * <pre>
     * filterNames = ["name", "age"]
     * Matches: getName(), isName(), setName(), getAge(), setAge()
     * </pre>
     *
     * @param listMethod  The list of methods to filter (will be modified if remove=true)
     * @param filterNames Array of field names to filter by
     * @param remove      true to remove matching methods, false to keep only matching methods
     * @return The filtered list (same instance if remove=true, new list if remove=false)
     */
    public static List<Method> filterList(List<Method> listMethod, String[] filterNames, boolean remove) {
        List<Method> methodsFiltered = new ArrayList<>();

        Arrays.stream(filterNames).forEach(name -> {
            Optional<Method> methodToFilter = listMethod.stream()
                    .filter(method -> isMethodMatchingFieldName(method, name))
                    .findAny();
            methodToFilter.ifPresent(methodsFiltered::add);
        });

        if (!CollectionUtils.isEmpty(methodsFiltered)) {
            if (remove) {
                listMethod.removeAll(methodsFiltered);
            } else {
                return methodsFiltered;
            }
        }
        return listMethod;
    }

    /**
     * Checks if a method name matches a field name (as getter or setter).
     * <p>
     * Handles:
     * <ul>
     *   <li>get + FieldName</li>
     *   <li>is + FieldName (for booleans)</li>
     *   <li>set + FieldName</li>
     * </ul>
     *
     * @param method    The method to check
     * @param fieldName The field name to match
     * @return true if method name matches the field name pattern
     */
    private static boolean isMethodMatchingFieldName(Method method, String fieldName) {
        String methodName = method.getName();
        return methodName.equalsIgnoreCase("get" + fieldName) ||
                methodName.equalsIgnoreCase("is" + fieldName) ||
                methodName.equalsIgnoreCase("set" + fieldName);
    }

    // ==================== Safe Getter Operations ====================

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
     * @throws ApiException if getter function is null
     */
    public static <T, R> Optional<R> safeGet(T obj, Function<T, R> getter) {
        validateGetter(getter);

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
     * @throws ApiException if getter function is null
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
    public static <T> List<T> removeNulls(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        return list.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // ==================== Dynamic Getter/Setter Invocation ====================

    /**
     * Gets the value of a field using its getter method.
     * <p>
     * This method dynamically constructs a getter name based on the field name:
     * <ul>
     *   <li>For boolean fields: isFieldName()</li>
     *   <li>For other fields: getFieldName()</li>
     * </ul>
     * <p>
     * The getter must be public to be invoked.
     * <p>
     * <strong>Example:</strong>
     * <pre>
     * Field nameField = User.class.getDeclaredField("name");
     * Object value = getValueDynamicallyThroughGetterNameFromField(nameField, user);
     * // Invokes user.getName()
     * </pre>
     *
     * @param <T>          The type of the object containing the field
     * @param field        The field to get the value for (must not be null)
     * @param getterObject The object containing the field (must not be null)
     * @return The value of the field
     * @throws ApiException If the getter cannot be found or invoked, or if parameters are null
     */
    public static <T> Object getValueDynamicallyThroughGetterNameFromField(Field field, T getterObject) {
        validateField(field);
        validateObject(getterObject, "Getter object");

        String getterPrefix = field.getType() == boolean.class ? "is" : "get";
        String getterName = getterPrefix + StringUtils.capitalize(field.getName());

        return getValueDynamicallyThroughGetterName(getterName, getterObject);
    }

    /**
     * Gets a value by invoking a getter method by name.
     * <p>
     * The getter must:
     * <ul>
     *   <li>Exist in the object's class or its hierarchy</li>
     *   <li>Be public</li>
     *   <li>Take no parameters</li>
     * </ul>
     * <p>
     * <strong>Example:</strong>
     * <pre>
     * Object value = getValueDynamicallyThroughGetterName("getName", user);
     * // Invokes user.getName()
     * </pre>
     *
     * @param <T>          The type of the object containing the getter
     * @param getterName   The name of the getter method (must not be null or empty)
     * @param getterObject The object containing the getter (must not be null)
     * @return The value returned by the getter
     * @throws ApiException If the getter cannot be found, is not public, or invocation fails
     */
    public static <T> Object getValueDynamicallyThroughGetterName(String getterName, T getterObject) {
        validateMethodName(getterName, "Getter");
        validateObject(getterObject, "Getter object");

        Method getter = findGetterMethod(getterName, getterObject);

        if (!Modifier.isPublic(getter.getModifiers())) {
            throw new ApiException("Getter method is not public: " + getterName);
        }

        try {
            return getter.invoke(getterObject);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ApiException("Error invoking getter: " + getterName, e);
        }
    }

    /**
     * Sets the value of a field using its setter method.
     * <p>
     * This method dynamically constructs a setter name based on the field name:
     * setFieldName()
     * <p>
     * The setter must be public to be invoked. Handles type conversions between
     * primitives and their wrapper types automatically.
     * <p>
     * <strong>Example:</strong>
     * <pre>
     * Field ageField = User.class.getDeclaredField("age");
     * setValueDynamicallyThroughSetterNameFromField(ageField, user, 30);
     * // Invokes user.setAge(30)
     * </pre>
     *
     * @param <T>   The type of the object containing the field
     * @param <S>   The type of the value to set
     * @param field The field to set (must not be null)
     * @param dest  The object containing the field (must not be null)
     * @param value The value to set (can be null)
     * @throws ApiException If the setter cannot be found or invoked, or if parameters are invalid
     */
    public static <T, S> void setValueDynamicallyThroughSetterNameFromField(Field field, T dest, S value) {
        validateField(field);
        validateObject(dest, "Destination object");

        String setterName = "set" + StringUtils.capitalize(field.getName());
        setValueDynamicallyThroughSetterName(setterName, dest, value);
    }

    /**
     * Sets a value by invoking a setter method by name.
     * <p>
     * The setter must:
     * <ul>
     *   <li>Exist in the object's class or its hierarchy</li>
     *   <li>Be public</li>
     *   <li>Take exactly one parameter</li>
     * </ul>
     * <p>
     * Automatically handles type conversions:
     * <ul>
     *   <li>Primitive ↔ Wrapper (int ↔ Integer)</li>
     *   <li>Null values: Sets primitive default values (0, false, etc.)</li>
     * </ul>
     * <p>
     * <strong>Example:</strong>
     * <pre>
     * setValueDynamicallyThroughSetterName("setAge", user, 30);
     * // Invokes user.setAge(30)
     * </pre>
     *
     * @param <T>        The type of the object containing the setter
     * @param <S>        The type of the value to set
     * @param setterName The name of the setter method (must not be null or empty)
     * @param target     The object containing the setter (must not be null)
     * @param valueToSet The value to set (can be null)
     * @throws ApiException If the setter cannot be found, is not public, or invocation fails
     */
    public static <T, S> void setValueDynamicallyThroughSetterName(String setterName, T target, S valueToSet) {
        validateMethodName(setterName, "Setter");
        validateObject(target, "Target object");

        Method setter = findSetterMethod(setterName, target);

        if (!Modifier.isPublic(setter.getModifiers())) {
            throw new ApiException("Setter method is not public: " + setterName);
        }

        Class<?> paramType = setter.getParameterTypes()[0];
        Class<?> valueType = valueToSet != null ? valueToSet.getClass() : null;

        try {
            invokeSetterWithTypeConversion(setter, target, valueToSet, paramType, valueType);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new ApiException("Error invoking setter: " + setterName, e);
        }
    }

    /**
     * Invokes setter with automatic type conversion handling.
     * <p>
     * Handles:
     * <ul>
     *   <li>Null values: converts to primitive defaults</li>
     *   <li>Primitive ↔ Wrapper conversions</li>
     *   <li>Direct assignment when types match</li>
     * </ul>
     *
     * @param setter     The setter method to invoke
     * @param target     The target object
     * @param valueToSet The value to set
     * @param paramType  The setter's parameter type
     * @param valueType  The value's actual type
     * @throws IllegalAccessException    If setter cannot be accessed
     * @throws InvocationTargetException If setter invocation fails
     * @throws ApiException              If types are incompatible
     */
    private static <T, S> void invokeSetterWithTypeConversion(
            Method setter,
            T target,
            S valueToSet,
            Class<?> paramType,
            Class<?> valueType) throws IllegalAccessException, InvocationTargetException {

        // Handle null values
        if (valueToSet == null) {
            if (paramType.isPrimitive()) {
                Object defaultValue = ReflectionTypeUtil.defaultValueFor(paramType);
                setter.invoke(target, defaultValue);
                return;
            } else {
                setter.invoke(target, (Object) null);
                return;
            }
        }

        // Handle primitive to wrapper conversion
        if (paramType.isPrimitive()) {
            Class<?> wrapperType = ClassUtils.primitiveToWrapper(paramType);
            if (wrapperType.isAssignableFrom(valueType)) {
                setter.invoke(target, valueToSet);
                return;
            }
        }

        // Handle wrapper to primitive conversion
        if (valueType != null) {
            Class<?> wrapperType = ClassUtils.primitiveToWrapper(valueType);
            if (wrapperType.isAssignableFrom(paramType)) {
                setter.invoke(target, valueToSet);
                return;
            }
        }

        // Direct assignment if types match
        if (valueType != null && paramType.isAssignableFrom(valueType)) {
            setter.invoke(target, valueToSet);
            return;
        }

        throw new ApiException(String.format(
                "Incompatible parameter type for setter: expected %s but got %s",
                paramType.getName(),
                valueType != null ? valueType.getName() : "null"
        ));
    }

    // ==================== Helper Methods - Method Finding ====================

    /**
     * Finds a getter method by name in the object's class hierarchy.
     *
     * @param <T>          The type of the object
     * @param getterName   The name of the getter
     * @param getterObject The object to search in
     * @return The getter method
     * @throws ApiException If getter is not found or no getters exist
     */
    private static <T> Method findGetterMethod(String getterName, T getterObject) {
        List<Method> allGetters = findGetMethods(getterObject);

        if (CollectionUtils.isEmpty(allGetters)) {
            throw new ApiException("There's no getter method in specified Object!");
        }

        Optional<Method> opGetter = allGetters.stream()
                .filter(getter -> getter.getName().equalsIgnoreCase(getterName))
                .findAny();

        if (opGetter.isEmpty()) {
            throw new ApiException("There's no getter with specified name: " + getterName);
        }

        return opGetter.get();
    }

    /**
     * Finds a setter method by name in the object's class hierarchy.
     *
     * @param <T>         The type of the object
     * @param setterName  The name of the setter
     * @param setterClass The object to search in
     * @return The setter method
     * @throws ApiException If setter is not found or no setters exist
     */
    private static <T> Method findSetterMethod(String setterName, T setterClass) {
        List<Method> allSetters = findSetMethods(setterClass);

        if (CollectionUtils.isEmpty(allSetters)) {
            throw new ApiException("There's no setter method in specified Object!");
        }

        Optional<Method> opSetter = allSetters.stream()
                .filter(setter -> setter.getName().equalsIgnoreCase(setterName))
                .findAny();

        if (opSetter.isEmpty()) {
            throw new ApiException("There's no setter with specified name: " + setterName);
        }

        return opSetter.get();
    }

    // ==================== Validation Methods ====================

    /**
     * Validates that an object is not null.
     *
     * @param object      The object to validate
     * @param description Description for the error message
     * @throws ApiException if object is null
     */
    private static void validateObject(Object object, String description) {
        if (object == null) {
            throw new ApiException(description + " cannot be null");
        }
    }

    /**
     * Validates that a field is not null.
     *
     * @param field The field to validate
     * @throws ApiException if field is null
     */
    private static void validateField(Field field) {
        if (field == null) {
            throw new ApiException("Field cannot be null");
        }
    }

    /**
     * Validates that a method name is not null or empty.
     *
     * @param methodName  The method name to validate
     * @param description Description for the error message (e.g., "Getter", "Setter")
     * @throws ApiException if method name is null or empty
     */
    private static void validateMethodName(String methodName, String description) {
        if (methodName == null || methodName.trim().isEmpty()) {
            throw new ApiException(description + " method name cannot be null or empty");
        }
    }

    /**
     * Validates that a getter function is not null.
     *
     * @param getter The getter function to validate
     * @throws ApiException if getter is null
     */
    private static <T, R> void validateGetter(Function<T, R> getter) {
        if (getter == null) {
            throw new ApiException("Getter function cannot be null");
        }
    }

    /**
     * Validates parameters for object comparison.
     * <p>
     * Checks that:
     * <ul>
     *   <li>Both entities are not null</li>
     *   <li>Both entities are of the same type</li>
     * </ul>
     *
     * @param entity1 First entity to validate
     * @param entity2 Second entity to validate
     * @param <T>     The type of entities
     * @throws ApiException if entities are null or of different types
     */
    private static <T> void validateComparisonParameters(T entity1, T entity2) {
        if (entity1 == null || entity2 == null) {
            throw new ApiException(NULL_ENTITY_ERROR);
        }
        if (!entity1.getClass().equals(entity2.getClass())) {
            throw new ApiException(DIFFERENT_TYPES_ERROR);
        }
    }
}