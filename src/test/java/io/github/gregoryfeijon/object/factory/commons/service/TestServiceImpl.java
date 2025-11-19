package io.github.gregoryfeijon.object.factory.commons.service;

public class TestServiceImpl implements TestServiceInterface {
    private final String name;

    public TestServiceImpl() {
        this("default");
    }

    public TestServiceImpl(String name) {
        this.name = name;
    }

    @Override
    public String execute() {
        return "Executed: " + name;
    }

    public String getName() {
        return name;
    }
}