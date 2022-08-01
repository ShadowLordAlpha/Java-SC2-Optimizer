package com.shadowcs.optimizer.pojo;

import lombok.NonNull;

import java.util.HashMap;
import java.util.function.Function;

public class LoadingHashMap<K, V> extends HashMap<K, V> {

    // As these are far more likely to grow we define a different initial then the default
    private static final int INITIAL_CAPCITY = 256;
    private final Function<K, V> builder;

    public LoadingHashMap(@NonNull Function<K, V> builder) {
        super(INITIAL_CAPCITY);

        this.builder = builder;
    }

    @Override
    public V get(Object key) {
        return computeIfAbsent((K) key);
    }

    public V computeIfAbsent(K key) {
        return computeIfAbsent(key, builder);
    }

    public V getIfPresent(K key) {
        return super.get(key);
    }
}
