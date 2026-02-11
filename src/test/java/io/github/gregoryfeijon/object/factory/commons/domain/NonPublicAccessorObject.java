package io.github.gregoryfeijon.object.factory.commons.domain;

public class NonPublicAccessorObject {

    private String value = "secret";

    protected String getValue() {
        return value;
    }

    protected void setValue(String value) {
        this.value = value;
    }
}
