package io.github.gregoryfeijon.object.factory.commons.domain.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Creates an enum with nullable values for edge case testing
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum NullableTestEnum {
    WITH_VALUE("value"),
    WITH_NULL(null),
    WITH_EMPTY("");

    private final String value;
}