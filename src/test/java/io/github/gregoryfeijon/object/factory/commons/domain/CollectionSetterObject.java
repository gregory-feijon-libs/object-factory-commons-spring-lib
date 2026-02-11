package io.github.gregoryfeijon.object.factory.commons.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
public class CollectionSetterObject {

    private Collection<String> items;
    private Object genericValue;
    private List<Integer> numbers;
}
