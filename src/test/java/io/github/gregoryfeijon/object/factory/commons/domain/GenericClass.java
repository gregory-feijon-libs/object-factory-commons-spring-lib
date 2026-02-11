package io.github.gregoryfeijon.object.factory.commons.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class GenericClass<T, U> {

    private T field1;
    private List<U> field2;
    private Map<String, T> field3;
}
