package io.github.gregoryfeijon.object.factory.commons.domain;

import java.util.List;

/**
 * A record with nested complex types for testing.
 */
public record NestedRecord(String id, List<String> tags, TestRecord innerRecord) {
}
