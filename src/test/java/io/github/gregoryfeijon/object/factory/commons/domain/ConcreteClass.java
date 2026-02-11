package io.github.gregoryfeijon.object.factory.commons.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConcreteClass extends GenericClass<String, Integer> {

    private String specificField;
}
