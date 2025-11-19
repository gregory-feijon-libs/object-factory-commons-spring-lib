package io.github.gregoryfeijon.object.factory.commons.domain.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum NullValueEnum {
    WITH_VALUE("value"),
    WITH_NULL(null);

    private final String value;
}