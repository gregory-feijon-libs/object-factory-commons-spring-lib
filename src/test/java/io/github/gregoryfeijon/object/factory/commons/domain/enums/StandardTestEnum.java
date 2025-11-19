package io.github.gregoryfeijon.object.factory.commons.domain.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Creates a standard test enum for testing EnumUtil
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum StandardTestEnum {
    FIRST("F", 1, true),
    SECOND("S", 2, false),
    THIRD("T", 3, true);

    private final String code;
    private final Integer id;
    private final boolean active;
}