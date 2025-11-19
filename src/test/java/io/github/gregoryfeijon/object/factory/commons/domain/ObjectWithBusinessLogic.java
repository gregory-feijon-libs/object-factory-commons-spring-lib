package io.github.gregoryfeijon.object.factory.commons.domain;

public class ObjectWithBusinessLogic {

    private String value;
    private int setterCallCount = 0;

    public void setValue(String value) {
        this.setterCallCount++;
        this.value = value != null ? value.toUpperCase() : null;
    }

    public String getValue() {
        return value;
    }

    public int getSetterCallCount() {
        return setterCallCount;
    }
}