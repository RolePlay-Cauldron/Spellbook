package com.github.roleplaycauldron.spellbook.core.cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * A loading cache that keeps one canonical entity instance indexed by multiple keys.
 * <p>
 * The cache maintains a single canonical entry per entity and exposes one or more typed
 * {@link KeySpace key spaces} to access that entry. Each key space defines how to extract a key
 * from an entity and how to load an entity from that key when the cache misses.
 * <p>
 * Internally, this avoids the usual reliability problems of maintaining multiple independent
 * caches for the same value. All aliases for an entity are inserted, replaced, and invalidated together.
 *
 * @param <E> the entity type
 */
public final class MultiKeyLoadingCache<E> {

    private final List<KeySpace<?, E>> keySpaces;

    private final Map<KeySpace<?, E>, ConcurrentMap<Object, CacheEntry<E>>> indexes;

    private final ConcurrentMap<CacheEntry<E>, Boolean> entries;

    private final ReentrantLock mutationLock;

    private MultiKeyLoadingCache(final List<KeySpace<?, E>> keySpaces) {
        this.keySpaces = keySpaces;
        this.indexes = new LinkedHashMap<>();
        this.entries = new ConcurrentHashMap<>();
        this.mutationLock = new ReentrantLock();

        for (final KeySpace<?, E> keySpace : keySpaces) {
            indexes.put(keySpace, new ConcurrentHashMap<>());
        }
    }

    /**
     * Create a new {@link MultiKeyLoadingCache} builder.
     *
     * @return the builder
     * @param <E> the entity type
     */
    public static <E> Builder<E> builder() {
        return new Builder<>();
    }

    /**
     * Resolve an entity by a key in the given key space.
     * <p>
     * If the key is not already cached, the key space loader is invoked. Successful loads are
     * indexed under every configured key space.
     *
     * @param keySpace the key space to query
     * @param key the key value
     * @param <K> the key type
     * @return the cached or loaded entity, or {@link Optional#empty()} if no entity exists
     */
    public <K> Optional<E> get(final KeySpace<K, E> keySpace, final K key) {
        requireKnownKeySpace(keySpace);
        Objects.requireNonNull(key, "key must not be null");

        final CacheEntry<E> cachedEntry = indexFor(keySpace).get(key);
        if (cachedEntry != null) {
            return Optional.of(cachedEntry.entity());
        }

        mutationLock.lock();
        try {
            final CacheEntry<E> cachedAfterLock = indexFor(keySpace).get(key);
            if (cachedAfterLock != null) {
                return Optional.of(cachedAfterLock.entity());
            }

            final Optional<E> loadedEntity = keySpace.load(key);
            return loadedEntity.map(e -> putInternal(e).entity());
        } finally {
            mutationLock.unlock();
        }
    }

    /**
     * Resolve an entity only if it is already cached.
     *
     * @param keySpace the key space to query
     * @param key the key value
     * @param <K> the key type
     * @return the cached entity, or {@link Optional#empty()} if the key is not cached
     */
    public <K> Optional<E> getIfPresent(final KeySpace<K, E> keySpace, final K key) {
        requireKnownKeySpace(keySpace);
        Objects.requireNonNull(key, "key must not be null");

        final CacheEntry<E> cachedEntry = indexFor(keySpace).get(key);
        return cachedEntry == null ? Optional.empty() : Optional.of(cachedEntry.entity());
    }

    /**
     * Insert or replace an entity in the cache.
     * <p>
     * The entity must provide a non-null key for every configured key space. If any existing cache
     * entry shares one of those keys, that old entry is removed before the new one is indexed.
     *
     * @param entity the entity to cache
     */
    public void put(final E entity) {
        Objects.requireNonNull(entity, "entity must not be null");

        mutationLock.lock();
        try {
            putInternal(entity);
        } finally {
            mutationLock.unlock();
        }
    }

    /**
     * Invalidate a cached entity by one of its keys.
     * <p>
     * If the key is present, every alias of the resolved entity is removed from the cache.
     *
     * @param keySpace the key space to use
     * @param key the key value
     * @param <K> the key type
     */
    public <K> void invalidate(final KeySpace<K, E> keySpace, final K key) {
        requireKnownKeySpace(keySpace);
        Objects.requireNonNull(key, "key must not be null");

        mutationLock.lock();
        try {
            final CacheEntry<E> entry = indexFor(keySpace).get(key);
            if (entry != null) {
                removeEntry(entry);
            }
        } finally {
            mutationLock.unlock();
        }
    }

    /**
     * Invalidate a cached entity by re-extracting all configured keys from the given entity.
     *
     * @param entity the entity to remove
     */
    public void invalidate(final E entity) {
        Objects.requireNonNull(entity, "entity must not be null");

        mutationLock.lock();
        try {
            final CacheEntry<E> entry = findExistingEntry(extractKeys(entity));
            if (entry != null) {
                removeEntry(entry);
            }
        } finally {
            mutationLock.unlock();
        }
    }

    /**
     * Remove every cached entity and every key index from this cache.
     */
    public void invalidateAll() {
        mutationLock.lock();
        try {
            entries.clear();
            for (final ConcurrentMap<Object, CacheEntry<E>> index : indexes.values()) {
                index.clear();
            }
        } finally {
            mutationLock.unlock();
        }
    }

    /**
     * Return the number of canonical entities currently cached.
     *
     * @return the number of cached entities
     */
    public long size() {
        return entries.size();
    }

    private <K> ConcurrentMap<Object, CacheEntry<E>> indexFor(final KeySpace<K, E> keySpace) {
        return indexes.get(keySpace);
    }

    private void requireKnownKeySpace(final KeySpace<?, E> keySpace) {
        Objects.requireNonNull(keySpace, "keySpace must not be null");
        if (!indexes.containsKey(keySpace)) {
            throw new IllegalArgumentException("Unknown key space: " + keySpace.name());
        }
    }

    private CacheEntry<E> putInternal(final E entity) {
        final CacheEntry<E> newEntry = new CacheEntry<>(entity, extractKeys(entity));
        final CacheEntry<E> existingEntry = findExistingEntry(newEntry.keys());

        if (existingEntry != null) {
            removeEntry(existingEntry);
        }

        for (final Map.Entry<KeySpace<?, E>, Object> entry : newEntry.keys().entrySet()) {
            final CacheEntry<E> conflictingEntry = indexes.get(entry.getKey()).get(entry.getValue());
            if (conflictingEntry != null) {
                removeEntry(conflictingEntry);
            }
        }

        entries.put(newEntry, Boolean.TRUE);
        for (final Map.Entry<KeySpace<?, E>, Object> entry : newEntry.keys().entrySet()) {
            indexes.get(entry.getKey()).put(entry.getValue(), newEntry);
        }
        return newEntry;
    }

    private Map<KeySpace<?, E>, Object> extractKeys(final E entity) {
        final Map<KeySpace<?, E>, Object> keys = new LinkedHashMap<>();
        for (final KeySpace<?, E> keySpace : keySpaces) {
            keys.put(keySpace, keySpace.extractKey(entity));
        }
        return Map.copyOf(keys);
    }

    private CacheEntry<E> findExistingEntry(final Map<KeySpace<?, E>, Object> keys) {
        for (final Map.Entry<KeySpace<?, E>, Object> entry : keys.entrySet()) {
            final CacheEntry<E> existingEntry = indexes.get(entry.getKey()).get(entry.getValue());
            if (existingEntry != null) {
                return existingEntry;
            }
        }
        return null;
    }

    private void removeEntry(final CacheEntry<E> entry) {
        if (entries.remove(entry) == null) {
            return;
        }

        for (final Map.Entry<KeySpace<?, E>, Object> keyEntry : entry.keys().entrySet()) {
            indexes.get(keyEntry.getKey()).remove(keyEntry.getValue(), entry);
        }
    }

    /**
     * Contains a cached entity and its keys.
     *
     * @param entity the entity
     * @param keys the extracted keys for each KeySpace
     * @param <E> the entity type
     */
    private record CacheEntry<E>(E entity, Map<KeySpace<?, E>, Object> keys) {
    }

    /**
     * Builder for a {@link MultiKeyLoadingCache}.
     *
     * @param <E> the entity type
     */
    public static final class Builder<E> {

        private final List<KeySpace<?, E>> keySpaces;

        private Builder() {
            this.keySpaces = new ArrayList<>();
        }

        /**
         * Register a new key space for the cache.
         *
         * @param name a unique descriptive name for the key space
         * @param keyExtractor extracts this key from an entity
         * @param loader loads an entity by this key
         * @param <K> the key type
         * @return the created key space handle used for cache lookups and invalidation
         */
        public <K> KeySpace<K, E> addKeySpace(final String name,
                                              final Function<E, K> keyExtractor,
                                              final Function<K, Optional<E>> loader) {
            if (keySpaces.stream().anyMatch(existingKeySpace -> existingKeySpace.name().equals(name))) {
                throw new IllegalArgumentException("Duplicate key space name: " + name);
            }
            final KeySpace<K, E> keySpace = new KeySpace<>(name, keyExtractor, loader);
            keySpaces.add(keySpace);
            return keySpace;
        }

        /**
         * Build the cache from the configured key spaces.
         *
         * @return a new multi-key cache
         * @throws IllegalStateException if fewer than two key spaces were configured
         */
        public MultiKeyLoadingCache<E> build() {
            if (keySpaces.size() < 2) {
                throw new IllegalStateException("A multi-key cache requires at least two key spaces");
            }
            return new MultiKeyLoadingCache<>(List.copyOf(keySpaces));
        }
    }

    /**
     * Describes one way to address entities in a {@link MultiKeyLoadingCache}.
     * <p>
     * A key space defines both the entity-side key extractor and the lookup-side loader for that
     * key type.
     *
     * @param <K> the key type
     * @param <E> the entity type
     */
    public static final class KeySpace<K, E> {

        private final String name;

        private final Function<E, K> keyExtractor;

        private final Function<K, Optional<E>> loader;

        private KeySpace(final String name,
                         final Function<E, K> keyExtractor,
                         final Function<K, Optional<E>> loader) {
            this.name = Objects.requireNonNull(name, "name must not be null");
            this.keyExtractor = Objects.requireNonNull(keyExtractor, "keyExtractor must not be null");
            this.loader = Objects.requireNonNull(loader, "loader must not be null");
        }

        /**
         * Return the descriptive name of this key space.
         *
         * @return the key space name
         */
        public String name() {
            return name;
        }

        private K extractKey(final E entity) {
            return Objects.requireNonNull(keyExtractor.apply(entity),
                    () -> "Key extractor for '" + name + "' returned null");
        }

        private Optional<E> load(final K key) {
            return Objects.requireNonNull(loader.apply(key),
                    () -> "Loader for '" + name + "' returned null");
        }
    }
}
