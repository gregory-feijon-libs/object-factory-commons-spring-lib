package io.github.gregoryfeijon.object.factory.commons.domain;

import lombok.Getter;
import lombok.Setter;

public class TestObject {

    @Getter
    @Setter
    private String privateField = "initial";

    @Getter
    @Setter
    protected Integer protectedField = 42;
    public Double publicField = 3.14;
    private final String finalField = "immutable";
}