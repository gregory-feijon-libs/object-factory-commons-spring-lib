package io.github.gregoryfeijon.object.factory.commons.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;

/**
 * 19/11/2025 às 03:11
 *
 * @author gregory.feijon
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestUtil {

    public static Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Field not found: " + fieldName, e);
        }
    }
}
