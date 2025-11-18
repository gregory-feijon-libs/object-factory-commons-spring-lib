package io.github.gregoryfeijon.object.factory.commons.utils;

import io.github.gregoryfeijon.object.factory.commons.service.AnotherTestService;
import io.github.gregoryfeijon.object.factory.commons.service.TestServiceImpl;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Test helper class providing reusable test fixtures and utilities
 * for reflection and utility class testing.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestObjectFactory {

    // ==================== Factory Methods ====================

    /**
     * Creates a TestServiceImpl with default name
     */
    public static TestServiceImpl createDefaultTestService() {
        return new TestServiceImpl();
    }

    /**
     * Creates a TestServiceImpl with custom name
     */
    public static TestServiceImpl createTestService(String name) {
        return new TestServiceImpl(name);
    }

    /**
     * Creates an AnotherTestService instance
     */
    public static AnotherTestService createAnotherTestService() {
        return new AnotherTestService();
    }
}