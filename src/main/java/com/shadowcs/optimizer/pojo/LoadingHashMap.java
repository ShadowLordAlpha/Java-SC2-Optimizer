package com.shadowcs.optimizer.pojo;

import java.util.HashMap;
import java.util.function.Function;

public class LoadingHashMap<K, V> extends HashMap<K, V> {

    private final Function<K, V> builder;

    public LoadingHashMap(Function<K, V> builder) {
        super();

        this.builder = builder;
    }

    @Override
    public V get(Object key) {
        return getOrLoad((K) key);
    }

    public V getOrLoad(K key) {
        V value = super.get(key);
        if(value == null && builder != null) {
            value = builder.apply(key);
            put(key, value);
        }

        return value;
    }

    public V getIfPresent(K key) {
        return super.get(key);
    }
}
