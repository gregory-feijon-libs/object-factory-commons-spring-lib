package io.github.gregoryfeijon.object.factory.commons.domain;

/**
 * Object with different return types for getters with similar names,
 * used for testing comparison edge cases where return types differ.
 */
public class MismatchedGetterObject {

    private String value;
    private long longValue;

    public MismatchedGetterObject(String value, long longValue) {
        this.value = value;
        this.longValue = longValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.longValue = longValue;
    }
}
