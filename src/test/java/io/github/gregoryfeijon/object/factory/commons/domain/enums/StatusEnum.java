package io.github.gregoryfeijon.object.factory.commons.domain.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum StatusEnum {
    ACTIVE("A", 1),
    INACTIVE("I", 2),
    PENDING("P", 3);

    private final String code;
    private final Integer id;
}