package com.github.roleplaycauldron.spellbook.core.cache;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Convenience wrapper for a {@link MultiKeyLoadingCache} with exactly two key spaces.
 * <p>
 * This type exists for the common case where one entity is addressed by exactly two identifiers
 * and a more specialized API is easier to read at call sites than working with explicit
 * {@link MultiKeyLoadingCache.KeySpace} handles.
 *
 * @param <K1> the first key type
 * @param <K2> the second key type
 * @param <E> the entity type
 */
public class BiKeyLoadingCache<K1, K2, E> {

    private final MultiKeyLoadingCache<E> delegate;

    private final MultiKeyLoadingCache.KeySpace<K1, E> firstKeySpace;

    private final MultiKeyLoadingCache.KeySpace<K2, E> secondKeySpace;

    /**
     * Create a two-key cache.
     *
     * @param firstKeyExtractor extracts the first key from an entity
     * @param loadByFirstKey loads an entity by its first key
     * @param secondKeyExtractor extracts the second key from an entity
     * @param loadBySecondKey loads an entity by its second key
     */
    public BiKeyLoadingCache(final Function<E, K1> firstKeyExtractor,
                             final Function<K1, Optional<E>> loadByFirstKey,
                             final Function<E, K2> secondKeyExtractor,
                             final Function<K2, Optional<E>> loadBySecondKey) {
        Objects.requireNonNull(firstKeyExtractor, "firstKeyExtractor must not be null");
        Objects.requireNonNull(loadByFirstKey, "loadByFirstKey must not be null");
        Objects.requireNonNull(secondKeyExtractor, "secondKeyExtractor must not be null");
        Objects.requireNonNull(loadBySecondKey, "loadBySecondKey must not be null");

        final MultiKeyLoadingCache.Builder<E> builder = MultiKeyLoadingCache.builder();
        this.firstKeySpace = builder.addKeySpace("key-1", firstKeyExtractor, loadByFirstKey);
        this.secondKeySpace = builder.addKeySpace("key-2", secondKeyExtractor, loadBySecondKey);
        this.delegate = builder.build();
    }

    /**
     * Resolve an entity by its first key, loading it if needed.
     *
     * @param key the first key
     * @return the cached or loaded entity, or {@link Optional#empty()} if none exists
     */
    public Optional<E> getByFirstKey(final K1 key) {
        return delegate.get(firstKeySpace, key);
    }

    /**
     * Resolve an entity by its second key, loading it if needed.
     *
     * @param key the second key
     * @return the cached or loaded entity, or {@link Optional#empty()} if none exists
     */
    public Optional<E> getBySecondKey(final K2 key) {
        return delegate.get(secondKeySpace, key);
    }

    /**
     * Look up an entity by its first key without triggering a load.
     *
     * @param key the first key
     * @return the cached entity, or {@link Optional#empty()} if it is not cached
     */
    public Optional<E> getIfPresentByFirstKey(final K1 key) {
        return delegate.getIfPresent(firstKeySpace, key);
    }

    /**
     * Look up an entity by its second key without triggering a load.
     *
     * @param key the second key
     * @return the cached entity, or {@link Optional#empty()} if it is not cached
     */
    public Optional<E> getIfPresentBySecondKey(final K2 key) {
        return delegate.getIfPresent(secondKeySpace, key);
    }

    /**
     * Insert or replace an entity and index it under both keys.
     *
     * @param entity the entity to cache
     */
    public void put(final E entity) {
        delegate.put(entity);
    }

    /**
     * Invalidate an entity by re-extracting both keys from it.
     *
     * @param entity the entity to remove
     */
    public void invalidate(final E entity) {
        delegate.invalidate(entity);
    }

    /**
     * Invalidate an entity by its first key.
     *
     * @param key the first key
     */
    public void invalidateByFirstKey(final K1 key) {
        delegate.invalidate(firstKeySpace, key);
    }

    /**
     * Invalidate an entity by its second key.
     *
     * @param key the second key
     */
    public void invalidateBySecondKey(final K2 key) {
        delegate.invalidate(secondKeySpace, key);
    }

    /**
     * Remove every cached entity from this cache.
     */
    public void invalidateAll() {
        delegate.invalidateAll();
    }

    /**
     * Return the number of cached entities.
     *
     * @return the cache size
     */
    public long size() {
        return delegate.size();
    }
}
