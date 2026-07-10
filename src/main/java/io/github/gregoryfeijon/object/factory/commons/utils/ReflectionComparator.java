package io.github.gregoryfeijon.object.factory.commons.utils;

import io.github.gregoryfeijon.object.factory.commons.exception.ApiException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for comparing objects by their getter values.
 * <p>
 * Extracted from {@link ReflectionUtil} to keep object-comparison concerns
 * (equality rules, field filtering) separate from method/field discovery.
 * <p>
 * <strong>Thread-Safety:</strong> All methods are stateless and thread-safe.
 *
 * @author gregory.feijon
 * @since 1.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionComparator {

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

        ReflectionValidationUtil.validateComparisonParameters(entity1, entity2);

        List<Method> getsEntity1 = ReflectionUtil.findGetMethods(entity1);
        List<Method> getsEntity2 = ReflectionUtil.findGetMethods(entity2);

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

        ReflectionValidationUtil.validateComparisonParameters(entity1, entity2);

        List<Method> getsEntity1 = ReflectionUtil.findGetMethods(entity1);
        List<Method> getsEntity2 = ReflectionUtil.findGetMethods(entity2);

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
            if (!compareMethodValues(methodEntity1, getsEntity2, entity1, entity2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares values from a single getter method between two entities.
     *
     * @param methodEntity1 The getter method from first entity
     * @param getsEntity2   All getter methods from second entity
     * @param entity1       First entity instance
     * @param entity2       Second entity instance
     * @return true if values are equal or method not found in entity2
     */
    private static boolean compareMethodValues(
            Method methodEntity1,
            List<Method> getsEntity2,
            Object entity1,
            Object entity2) throws InvocationTargetException, IllegalAccessException {

        Optional<Method> methodEntity2 = findMethodByName(getsEntity2, methodEntity1.getName());

        if (methodEntity2.isEmpty()) {
            return true;
        }

        Object value1 = methodEntity1.invoke(entity1);
        Object value2 = methodEntity2.get().invoke(entity2);

        return areValuesEqual(value1, value2, methodEntity1, methodEntity2.get());
    }

    /**
     * Finds a method by name in the given list.
     */
    private static Optional<Method> findMethodByName(List<Method> methods, String name) {
        return methods.stream()
                .filter(method -> method.getName().equalsIgnoreCase(name))
                .findAny();
    }

    /**
     * Determines if two values are equal considering type-specific equivalence rules.
     */
    private static boolean areValuesEqual(
            Object value1,
            Object value2,
            Method method1,
            Method method2) {

        if (ObjectUtils.nullSafeEquals(value1, value2)) {
            return true;
        }

        if (!areReturnTypesEqual(method1, method2)) {
            return false;
        }

        return areValuesEquivalent(value1, value2, method1.getReturnType());
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
        if (isNumberType(type)) {
            return areNumbersEquivalent(value1, value2);
        }
        if (type.isAssignableFrom(Collection.class)) {
            return areCollectionsEquivalent(value1, value2);
        }
        return false;
    }

    /**
     * Checks if a type represents a number (Integer or Double).
     *
     * @param type The type to check
     * @return true if the type is assignable from Integer or Double
     */
    private static boolean isNumberType(Class<?> type) {
        return type.isAssignableFrom(Integer.class) || type.isAssignableFrom(Double.class);
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
     * Checks if a string value is empty or null.
     *
     * @param value The string value to check
     * @return true if the string is null or empty
     */
    private static boolean isStringEmpty(Object value) {
        if (value == null) {
            return true;
        }
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
}
