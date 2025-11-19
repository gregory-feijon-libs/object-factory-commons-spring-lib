package io.github.gregoryfeijon.object.factory.commons.domain;

import lombok.Getter;
import lombok.Setter;

public class ComplexObject {

    @Getter
    @Setter
    private String stringValue;

    @Getter
    @Setter
    private Integer intValue;

    @Getter
    @Setter
    private boolean booleanValue;
    private Object nullValue;
}