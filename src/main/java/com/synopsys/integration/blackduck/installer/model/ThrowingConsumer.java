package com.synopsys.integration.blackduck.installer.model;

public interface ThrowingConsumer<T, E extends Throwable> {
    void accept(T t) throws E;

}
