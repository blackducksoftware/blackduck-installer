package com.synopsys.integration.blackduck.installer;

public interface BiThrowingFunction<T, R, E1 extends Throwable, E2 extends Throwable> {
    /**
     * Applies this function, which may throw two different exceptions, to the given argument.
     * @param t the function argument
     * @return the function result
     */
    R apply(T t) throws E1, E2;

}
