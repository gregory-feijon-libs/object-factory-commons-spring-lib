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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
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

@SuppressWarnings("java:S6204")
//warning do .toList() suprimida, uma vez que não se aplica nessa classe, que é uma classe útil
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtil {

    /**
     * Finds all getter methods of an object.
     * <p>
     * A method is considered a getter if its name starts with "get" or "is".
     *
     * @param object The object to find getters for
     * @return A list of getter methods
     */
    public static List<Method> findGetMethods(Object object) {
        return getMethodsAsList(object).stream().filter(method -> method.getName().toLowerCase().startsWith("get")
                || method.getName().toLowerCase().startsWith("is")).collect(Collectors.toList());
    }

    /**
     * Finds all setter methods of an object.
     * <p>
     * A method is considered a setter if its name starts with "set".
     *
     * @param object The object to find setters for
     * @return A list of setter methods
     */
    public static List<Method> findSetMethods(Object object) {
        return getMethodsAsList(object).stream()
                .filter(method -> method.getName().toLowerCase().startsWith("set"))
                .collect(Collectors.toList());
    }

    /**
     * Gets all methods of an object as a list.
     *
     * @param object The object to get methods for
     * @return A collection of all methods
     */
    public static Collection<Method> getMethodsAsList(Object object) {
        return Arrays.asList(ReflectionUtils.getAllDeclaredMethods(object.getClass()));
    }

    /**
     * Gets all fields of an object, including inherited fields.
     *
     * @param object The object to get fields for
     * @return A collection of fields
     */
    public static Collection<Field> getFieldsAsCollection(Object object) {
        return getFieldsAsCollection(object, true);
    }

    /**
     * Gets all fields of an object, including inherited fields.
     *
     * @param object The object to get fields for
     * @return A collection of fields
     */
    public static <T extends Collection<Field>> T getFieldsAsCollection(Object object, Supplier<T> collectionType) {
        return getFieldsAsCollection(object, true).stream().collect(Collectors.toCollection(collectionType));
    }

    /**
     * Gets all fields of an object, with an option to include inherited fields.
     *
     * @param object         The object to get fields for
     * @param includeParents Whether to include fields from parent classes
     * @return A collection of fields
     */
    public static Collection<Field> getFieldsAsCollection(Object object, boolean includeParents) {
        Class<?> clazz = object.getClass();
        Collection<Field> fields = Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.toList());
        if (includeParents && clazz.getSuperclass() != null) {
            clazz = clazz.getSuperclass();
            while (clazz != null) {
                fields.addAll(Arrays.stream(clazz.getDeclaredFields()).toList());
                clazz = clazz.getSuperclass();
            }

        }
        return fields;
    }

    /**
     * <strong>Método para fazer a comparação entre objetos de mesmo tipo. Se pelo
     * <p>
     * menos 1 for diferente, retorna false.</strong>
     *
     * @param <T>     - type of objects to compare
     * @param entity1 - &lt;T&gt;
     * @param entity2 - &lt;T&gt;
     * @return {@linkplain Boolean}
     * @throws InvocationTargetException - Exceção lançada se tiver algum erro na comparação das listas de métodos get
     * @throws IllegalAccessException    - Exceção lançada se tiver algum erro na comparação das listas de métodos get
     */
    public static <T> boolean compareObjectsValues(T entity1, T entity2) throws InvocationTargetException, IllegalAccessException {
        List<Method> getsEntity1 = ReflectionUtil.findGetMethods(entity1);
        List<Method> getsEntity2 = ReflectionUtil.findGetMethods(entity2);
        return compareLists(getsEntity1, getsEntity2, entity1, entity2);
    }

    /**
     * <strong>Método para fazer a comparação entre objetos de mesmo tipo. Se pelo
     * <p>
     * menos 1 for diferente, retorna false. Possui a opção de excluir campos da
     * <p>
     * comparação.</strong>
     *
     * @param <T>         - type of objects to compare
     * @param entity1     - &lt;T&gt;
     * @param entity2     - &lt;T&gt;
     * @param filterNames - {@linkplain String}[]
     * @return {@linkplain Boolean}
     * @throws InvocationTargetException - Execeção lançada se tiver algum erro na comparação dos objetos
     * @throws IllegalAccessException    - Execeção lançada se tiver algum erro na comparação dos objetos
     */
    public static <T> boolean compareObjectsValues(T entity1, T entity2, String[] filterNames)
            throws InvocationTargetException, IllegalAccessException {
        if (filterNames == null) {
            return compareObjectsValues(entity1, entity2);
        }
        return compare(entity1, entity2, filterNames, true);
    }

    /**
     * <strong>Método para fazer a comparação entre objetos de mesmo tipo. Se pelo
     * <p>
     * menos 1 for diferente, retorna false. Possui a opção de excluir campos da
     * <p>
     * comparação ou utilizar os campos especificados, excluindo os demais.</strong>
     *
     * @param <T>         - type of objects to compare
     * @param entity1     - &lt;T&gt;
     * @param entity2     - &lt;T&gt;
     * @param filterNames - {@linkplain String}[]
     * @param remove      {@linkplain Boolean} - true: exclui campos; false: utiliza
     *                    <p>
     *                    campos
     * @return {@linkplain Boolean}
     * @throws InvocationTargetException - Execeção lançada se tiver algum erro na comparação dos objetos
     * @throws IllegalAccessException    - Execeção lançada se tiver algum erro na comparação dos objetos
     */
    public static <T> boolean compareObjectsValues(T entity1, T entity2, String[] filterNames, boolean remove)
            throws InvocationTargetException, IllegalAccessException {
        return compare(entity1, entity2, filterNames, remove);
    }

    /**
     * <strong>Método que busca os getters e filtra, baseado nas configurações
     * <p>
     * estabelecidas.</strong>
     *
     * @param <T>         - type of objects to compare
     * @param entity1     - &lt;T&gt;
     * @param entity2     - &lt;T&gt;
     * @param filterNames - {@linkplain String}[]
     * @param remove      {@linkplain Boolean} - true: exclui campos; false: utiliza
     *                    <p>
     *                    campos
     * @return {@linkplain Boolean}
     * @throws InvocationTargetException - Execeção lançada se tiver algum erro na comparação das listas de getter
     * @throws IllegalAccessException    - Execeção lançada se tiver algum erro na comparação das listas de getter
     */
    private static <T> boolean compare(T entity1, T entity2, String[] filterNames, boolean remove)
            throws InvocationTargetException, IllegalAccessException {
        List<Method> getsEntity1 = ReflectionUtil.findGetMethods(entity1);
        List<Method> getsEntity2 = ReflectionUtil.findGetMethods(entity2);
        getsEntity1 = filterList(getsEntity1, filterNames, remove);
        getsEntity2 = filterList(getsEntity2, filterNames, remove);
        return compareLists(getsEntity1, getsEntity2, entity1, entity2);
    }

    /**
     * <strong>Método que efetivamente filtra a lista de getters, de acordo com os
     * <p>
     * parâmetros especificados.</strong>
     *
     * <p>
     * <p>
     * Filtra os métodos pelos prefixos get/is, referentes aos getters + cada um dos
     * <p>
     * nomes dos atributos (nomeação devida dos métodos getters, de acordo com as
     * <p>
     * boas práticas). Baseado no valor do {@linkplain Boolean remove}, retorna a
     * <p>
     * lista de todos os métodos sem os especificados, ou uma lista composta somente
     * <p>
     * por eles.
     *
     * <p>
     *
     * @param listMethod  - {@linkplain List}&lt;{@linkplain Method}&gt;
     * @param filterNames - {@linkplain String}[]
     * @param remove      - {@linkplain Boolean}
     * @return {@linkplain List}&lt;{@linkplain Method}&gt;
     */
    public static List<Method> filterList(List<Method> listMethod, String[] filterNames, boolean remove) {
        List<Method> methodsFiltered = new LinkedList<>();
        Arrays.stream(filterNames).forEach(name -> {
            Optional<Method> methodRemove = listMethod.stream()
                    .filter(method -> method.getName().equalsIgnoreCase("get" + name)
                            || method.getName().equalsIgnoreCase("is" + name))
                    .findAny();
            methodRemove.ifPresent(methodsFiltered::add);
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
     * <strong>Método que efetivamente compara as 2 listas de getters e retorna
     * <p>
     * true, caso não encontre nenhum valor diferente, ou false, caso
     * <p>
     * encontre.</strong>
     *
     * <p>
     * <p>
     * Para cada 1 dos métodos da lista 1, encontra o correspondente da lista 2
     * <p>
     * (garantido, pois é necessário que os objetos — dos quais os métodos são
     * <p>
     * extraídos — sejam do mesmo tipo) e obtem seus valores através do método
     * <p>
     * {@linkplain Method#invoke(Object, Object...) invoke}. É feita uma comparação
     * <p>
     * desses valores utilizando o método
     * <p>
     * {@linkplain ObjectUtils#nullSafeEquals(Object, Object) nullSafeEquals} e,
     * <p>
     * caso seja diferente e do tipo {@linkplain String}, uma segunda verificação,
     * <p>
     * pro caso de 1 ser null e a outra vazia, que, logicamente, são caracterizados
     * <p>
     * como valores iguais. Se todos os valores sejam iguais, retorna true, caso
     * <p>
     * contrário, retorna false.
     *
     * <p>
     *
     * @param getsEntity1 - {@linkplain List}&lt;{@linkplain Method}&gt;
     * @param getsEntity2 - {@linkplain List}&lt;{@linkplain Method}&gt;
     * @param entity1     - {@linkplain Object}
     * @param entity2     - {@linkplain Object}
     * @return {@linkplain Boolean}
     * @throws InvocationTargetException - Execeção lançada se tiver algum erro no invoke dos métodos get
     * @throws IllegalAccessException    - Execeção lançada se tiver algum erro no invoke dos métodos get
     */
    private static boolean compareLists(List<Method> getsEntity1, List<Method> getsEntity2, Object entity1,
                                        Object entity2) throws InvocationTargetException, IllegalAccessException {
        boolean retorno = true;
        for (Method methodEntity1 : getsEntity1) {
            Optional<Method> methodEntity2 = getsEntity2.stream()
                    .filter(method -> method.getName().equalsIgnoreCase(methodEntity1.getName()))
                    .findAny();
            if (methodEntity2.isPresent()) {
                Object valorSalvo = methodEntity1.invoke(entity1);
                Object valorUpdate = methodEntity2.get().invoke(entity2);
                if (!ObjectUtils.nullSafeEquals(valorSalvo, valorUpdate)) {
                    if (methodEntity1.getReturnType() == methodEntity2.get().getReturnType()) {
                        retorno = verificaTipoValor(valorSalvo, valorUpdate, methodEntity1.getReturnType());
                    } else {
                        retorno = false;
                    }
                }
            }
        }
        return retorno;
    }

    /**
     * Checks if collections need to be compared differently when one value is null.
     * <p>
     * When comparing collections where one is null and the other isn't,
     * this method checks if the non-null collection is empty.
     *
     * @param valorSalvo  The saved value (may be null)
     * @param valorUpdate The update value (may be null)
     * @return true if one value is null and the other is an empty collection
     */
    private static boolean verificaTipoValor(Object valorSalvo, Object valorUpdate, Class<?> returnType) {
        boolean retorno;
        if (returnType.isAssignableFrom(String.class)) {
            retorno = verificaStrings(valorSalvo, valorUpdate);
        } else if (returnType.isAssignableFrom(Integer.class) || returnType.isAssignableFrom(Double.class)) {
            retorno = verificaNumber(valorSalvo, valorUpdate);
        } else if (returnType.isAssignableFrom(Collection.class)) {
            retorno = verificaCollection(valorSalvo, valorUpdate);
        } else {
            retorno = false;
        }
        return retorno;
    }

    private static boolean verificaCollection(Object valorSalvo, Object valorUpdate) {
        boolean retorno = false;
        if (valorSalvo == null && valorUpdate != null) {
            retorno = isCollectionEmpty(valorUpdate);
        } else if (valorUpdate == null && valorSalvo != null) {
            retorno = isCollectionEmpty(valorSalvo);
        }
        return retorno;
    }

    /**
     * Checks if a collection is empty.
     *
     * @param valor The collection to check
     * @return true if the collection is empty, false otherwise
     */
    private static boolean isCollectionEmpty(Object valor) {
        return CollectionUtils.isEmpty((Collection<?>) valor);
    }

    /**
     * Checks if strings need to be compared differently when one value is null.
     * <p>
     * When comparing strings where one is null and the other isn't,
     * this method checks if the non-null string is empty.
     *
     * @param valorSalvo  The saved value (may be null)
     * @param valorUpdate The update value (may be null)
     * @return true if one value is null and the other is an empty string
     */
    private static boolean verificaStrings(Object valorSalvo, Object valorUpdate) {
        boolean retorno = false;
        if (valorSalvo == null && valorUpdate != null) {
            retorno = isValorEmpty(valorUpdate);
        } else if (valorUpdate == null && valorSalvo != null) {
            retorno = isValorEmpty(valorSalvo);
        }
        return retorno;
    }

    /**
     * Checks if a string is empty.
     *
     * @param valor The string to check
     * @return true if the string is empty, false otherwise
     */
    private static boolean isValorEmpty(Object valor) {
        return ((String) valor).isEmpty();
    }

    /**
     * Checks if numbers need to be compared differently when one value is null.
     * <p>
     * When comparing numbers where one is null and the other isn't,
     * this method checks if the non-null number is zero.
     *
     * @param valorSalvo  The saved value (may be null)
     * @param valorUpdate The update value (may be null)
     * @return true if one value is null and the other is zero
     */
    private static boolean verificaNumber(Object valorSalvo, Object valorUpdate) {
        boolean retorno = false;
        if (valorSalvo == null && valorUpdate != null) {
            retorno = isValorZero(valorUpdate);
        } else if (valorUpdate == null && valorSalvo != null) {
            retorno = isValorZero(valorSalvo);
        }
        return retorno;
    }

    /**
     * Checks if a number is zero.
     *
     * @param valorUpdate The number to check
     * @return true if the number is zero, false otherwise
     */
    private static boolean isValorZero(Object valorUpdate) {
        BigDecimal aux = BigDecimal.valueOf(((Number) valorUpdate).doubleValue());
        return aux.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Safely gets a value using a getter function, wrapping the result in an Optional.
     * <p>
     * This method handles null objects by returning an empty Optional.
     *
     * @param <T>    The type of the object
     * @param <R>    The type of the return value
     * @param obj    The object to get a value from
     * @param getter A function that extracts a value from the object
     * @return An Optional containing the value, or empty if the object is null or the value is null
     */
    public static <T, R> Optional<R> safeGet(T obj, Function<T, R> getter) {
        if (obj == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(getter.apply(obj));
    }

    /**
     * Safely gets a value using a getter function, returning a default value if null.
     * <p>
     * This method handles null objects by returning the default value.
     *
     * @param <T>          The type of the object
     * @param <R>          The type of the return value
     * @param obj          The object to get a value from
     * @param getter       A function that extracts a value from the object
     * @param defaultValue The default value to return if the object or value is null
     * @return The value from the getter, or the default value if null
     */
    public static <T, R> R safeGetWithDefaultValue(T obj, Function<T, R> getter, R defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        return safeGet(obj, getter).orElse(defaultValue);
    }

    /**
     * Removes null elements from a list.
     *
     * @param <T>  The type of elements in the list
     * @param list The list to remove nulls from
     * @return A new list with null elements removed
     */
    public static <T> List<T> removeNulls(List<T> list) {
        return CollectionUtils.isEmpty(list) ? List.of() :
                list.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
    }

    /**
     * Gets the value of a field using its getter method.
     * <p>
     * This method dynamically constructs a getter name based on the field name
     * (using "get" or "is" prefix) and invokes it.
     *
     * @param <T>          The type of the object containing the field
     * @param field        The field to get the value for
     * @param getterObject The object containing the field
     * @return The value of the field
     * @throws ApiException If the getter cannot be found or invoked
     */
    public static <T> Object getValueDynamicallyThroughGetterNameFromField(Field field, T getterObject) {
        String getterPrefix = field.getType() == boolean.class ? "is" : "get";

        String getterName = getterPrefix + StringUtils.capitalize(field.getName());
        return getValueDynamicallyThroughGetterName(getterName, getterObject);
    }

    /**
     * Gets a value by invoking a getter method by name.
     *
     * @param <T>          The type of the object containing the getter
     * @param getterName   The name of the getter method
     * @param getterObject The object containing the getter
     * @return The value returned by the getter
     * @throws ApiException If the getter cannot be found or invoked
     */
    public static <T> Object getValueDynamicallyThroughGetterName(String getterName, T getterObject) {
        var getter = findGetterMethod(getterName, getterObject);
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
     * Finds a getter method by name.
     *
     * @param <T>          The type of the object containing the getter
     * @param getterName   The name of the getter method
     * @param getterObject The object containing the getter
     * @return The getter method
     * @throws ApiException If the getter cannot be found
     */
    private static <T> Method findGetterMethod(String getterName, T getterObject) {
        var allGetters = findGetMethods(getterObject);
        if (CollectionUtils.isEmpty(allGetters)) {
            throw new ApiException("There's no getter method in specified Object!");
        }
        var opGetter = allGetters.stream()
                .filter(getter -> getter.getName().equalsIgnoreCase(getterName))
                .findAny();
        if (opGetter.isEmpty()) {
            throw new ApiException("There's no getter with specified name: " + getterName);
        }
        return opGetter.get();
    }

    /**
     * Set the value of a field using its setter method.
     * <p>
     * This method dynamically constructs a setter name based on the field name
     * (using "set" prefix) and invokes it.
     *
     * @param <T>         The type of the object containing the setter
     * @param <S>         The type of the value to set
     * @param field       The field to set the value
     * @param setterClass The object containing the setter
     * @param valueToSet  The value to set
     * @throws ApiException If the setter cannot be found or invoked
     */
    public static <T, S> void setValueDynamicallyThroughSetterNameFromField(Field field, T setterClass, S valueToSet) {
        String setterName = "set" + StringUtils.capitalize(field.getName());
        setValueDynamicallyThroughSetterName(setterName, setterClass, valueToSet);
    }

    /**
     * Sets a value by invoking a setter method by name.
     *
     * @param <T>        The type of the object containing the setter
     * @param <S>        The type of the value to set
     * @param setterName The name of the setter method
     * @param target     The object containing the setter
     * @param valueToSet The value to set
     * @throws ApiException If the setter cannot be found or invoked
     */
    public static <T, S> void setValueDynamicallyThroughSetterName(String setterName, T target, S valueToSet) {
        Method setter = findSetterMethod(setterName, target);

        if (!Modifier.isPublic(setter.getModifiers())) {
            throw new ApiException("Setter method is not public: " + setterName);
        }

        Class<?> paramType = setter.getParameterTypes()[0];
        Class<?> valueType = valueToSet != null ? valueToSet.getClass() : null;

        try {
            verifyTypes(setterName, target, valueToSet, paramType, setter, valueType);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new ApiException("Error invoking setter: " + setterName, e);
        }
    }

    /**
     * Verifies type compatibility and invokes the setter accordingly.
     */
    private static <T, S> void verifyTypes(
            String setterName, T target, S valueToSet,
            Class<?> paramType, Method setter, Class<?> valueType
    ) throws IllegalAccessException, InvocationTargetException {

        if (verifyNullValueToSet(target, valueToSet, paramType, setter)) {
            return;
        }

        if (verifyPrimitiveWrapperCompatibility(target, valueToSet, paramType, valueType, setter, true)) {
            return;
        }

        if (verifyPrimitiveWrapperCompatibility(target, valueToSet, paramType, valueType, setter, false)) {
            return;
        }

        if (valueType != null && paramType.isAssignableFrom(valueType)) {
            setter.invoke(target, valueToSet);
            return;
        }

        throw new ApiException(String.format(
                "Incompatible parameter type for setter '%s': expected %s but got %s",
                setterName,
                paramType.getName(),
                valueType != null ? valueType.getName() : "null"
        ));
    }

    /**
     * Handles conversion between primitive <-> wrapper types.
     *
     * @param target         the target instance
     * @param valueToSet     the value to set
     * @param paramType      setter parameter type
     * @param valueType      type of the provided value
     * @param setter         method reference
     * @param paramToWrapper if true, converts paramType → wrapperType; if false, converts valueType → wrapperType
     */
    private static <T, S> boolean verifyPrimitiveWrapperCompatibility(
            T target, S valueToSet,
            Class<?> paramType, Class<?> valueType,
            Method setter, boolean paramToWrapper
    ) throws IllegalAccessException, InvocationTargetException {

        if (valueType == null) return false;

        Class<?> convertedType = paramToWrapper
                ? ClassUtils.primitiveToWrapper(paramType)
                : ClassUtils.primitiveToWrapper(valueType);

        Class<?> comparableType = paramToWrapper ? valueType : paramType;

        if (convertedType.isAssignableFrom(comparableType)) {
            setter.invoke(target, valueToSet);
            return true;
        }
        return false;
    }

    private static <T, S> boolean verifyNullValueToSet(T target, S valueToSet, Class<?> paramType, Method setter) throws IllegalAccessException, InvocationTargetException {
        if (valueToSet == null) {
            if (paramType.isPrimitive()) {
                Object defaultValue = ReflectionTypeUtil.defaultValueFor(paramType);
                setter.invoke(target, defaultValue);
                return true;
            } else {
                setter.invoke(target, (Object) null);
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a setter method by name.
     *
     * @param <T>         The type of the object containing the setter
     * @param setterName  The name of the setter method
     * @param setterClass The object containing the setter
     * @return The setter method
     * @throws ApiException If the setter cannot be found
     */
    private static <T> Method findSetterMethod(String setterName, T setterClass) {
        var allSetters = findSetMethods(setterClass);
        if (CollectionUtils.isEmpty(allSetters)) {
            throw new ApiException("There's no setter method in specified Object!");
        }
        var opSetter = allSetters.stream()
                .filter(setter -> setter.getName().equalsIgnoreCase(setterName))
                .findAny();
        if (opSetter.isEmpty()) {
            throw new ApiException("There's no setter with specified name: " + setterName);
        }
        return opSetter.get();
    }
}