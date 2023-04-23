package de.malkusch.ha.automation.infrastructure;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LazyValue<T> {

    private final Fetch<T> fetch;
    private T value;

    public T lazy() throws Exception {
        if (value == null) {
            value = fetch.fetch();
        }
        return value;
    }

    @FunctionalInterface
    public static interface Fetch<T> {
        T fetch() throws Exception;
    }
}
