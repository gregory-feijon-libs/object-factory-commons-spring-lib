package io.github.gregoryfeijon.object.factory.commons.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

@Getter
@AllArgsConstructor
public class ComparisonObject {
    private String stringValue;
    private Integer intValue;
    private Double doubleValue;
    private Collection<String> collectionValue;
}