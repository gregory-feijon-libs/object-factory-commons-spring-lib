package io.github.gregoryfeijon.object.factory.commons.config;

import io.github.gregoryfeijon.object.factory.commons.service.AnotherTestService;
import io.github.gregoryfeijon.object.factory.commons.service.TestServiceImpl;
import io.github.gregoryfeijon.object.factory.commons.service.TestServiceInterface;
import io.github.gregoryfeijon.object.factory.commons.utils.factory.FactoryUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class FactoryUtilTestConfig {

    @Bean
    public FactoryUtil factoryUtil() {
        return new FactoryUtil();
    }

    @Bean("testService")
    public TestServiceInterface testService() {
        return new TestServiceImpl("test");
    }

    @Bean("anotherTestService")
    public AnotherTestService anotherTestService() {
        return new AnotherTestService();
    }
}