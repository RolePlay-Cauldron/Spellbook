package com.github.roleplaycauldron.spellbook.core.cache;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class MultiKeyLoadingCacheTest {

    @Test
    void getIndexesEntityAcrossAllConfiguredKeys() {
        final TestEntity entity = new TestEntity("1", "alpha", "alpha@example.test");
        final TestRepository repository = new TestRepository(entity);

        final MultiKeyLoadingCache.Builder<TestEntity> builder = MultiKeyLoadingCache.builder();
        final MultiKeyLoadingCache.KeySpace<String, TestEntity> idKey =
                builder.addKeySpace("id", TestEntity::id, repository::findById);
        final MultiKeyLoadingCache.KeySpace<String, TestEntity> usernameKey =
                builder.addKeySpace("username", TestEntity::username, repository::findByUsername);
        final MultiKeyLoadingCache.KeySpace<String, TestEntity> emailKey =
                builder.addKeySpace("email", TestEntity::email, repository::findByEmail);
        final MultiKeyLoadingCache<TestEntity> cache = builder.build();

        final Optional<TestEntity> loaded = cache.get(emailKey, entity.email());

        assertEquals(Optional.of(entity), loaded);
        assertEquals(Optional.of(entity), cache.getIfPresent(idKey, entity.id()));
        assertEquals(Optional.of(entity), cache.getIfPresent(usernameKey, entity.username()));
        assertEquals(1L, cache.size());
    }

    @Test
    void putReplacesOldAliasesForTheSameEntity() {
        final MultiKeyLoadingCache.Builder<TestEntity> builder = MultiKeyLoadingCache.builder();
        final MultiKeyLoadingCache.KeySpace<String, TestEntity> idKey =
                builder.addKeySpace("id", TestEntity::id, key -> Optional.empty());
        final MultiKeyLoadingCache.KeySpace<String, TestEntity> usernameKey =
                builder.addKeySpace("username", TestEntity::username, key -> Optional.empty());
        final MultiKeyLoadingCache.KeySpace<String, TestEntity> emailKey =
                builder.addKeySpace("email", TestEntity::email, key -> Optional.empty());
        final MultiKeyLoadingCache<TestEntity> cache = builder.build();

        final TestEntity oldEntity = new TestEntity("1", "alpha", "alpha@example.test");
        final TestEntity updatedEntity = new TestEntity("1", "bravo", "bravo@example.test");

        cache.put(oldEntity);
        cache.put(updatedEntity);

        assertEquals(Optional.empty(), cache.getIfPresent(usernameKey, oldEntity.username()));
        assertEquals(Optional.empty(), cache.getIfPresent(emailKey, oldEntity.email()));
        assertEquals(Optional.of(updatedEntity), cache.getIfPresent(idKey, updatedEntity.id()));
        assertEquals(Optional.of(updatedEntity), cache.getIfPresent(usernameKey, updatedEntity.username()));
        assertEquals(Optional.of(updatedEntity), cache.getIfPresent(emailKey, updatedEntity.email()));
        assertEquals(1L, cache.size());
    }

    @Test
    void conflictingAliasRemovesThePreviouslyIndexedEntity() {
        final MultiKeyLoadingCache.Builder<TestEntity> builder = MultiKeyLoadingCache.builder();
        final MultiKeyLoadingCache.KeySpace<String, TestEntity> idKey =
                builder.addKeySpace("id", TestEntity::id, key -> Optional.empty());
        final MultiKeyLoadingCache.KeySpace<String, TestEntity> usernameKey =
                builder.addKeySpace("username", TestEntity::username, key -> Optional.empty());
        final MultiKeyLoadingCache.KeySpace<String, TestEntity> emailKey =
                builder.addKeySpace("email", TestEntity::email, key -> Optional.empty());
        final MultiKeyLoadingCache<TestEntity> cache = builder.build();

        final TestEntity firstEntity = new TestEntity("1", "shared", "first@example.test");
        final TestEntity secondEntity = new TestEntity("2", "shared", "second@example.test");

        cache.put(firstEntity);
        cache.put(secondEntity);

        assertEquals(Optional.empty(), cache.getIfPresent(idKey, firstEntity.id()));
        assertEquals(Optional.of(secondEntity), cache.getIfPresent(idKey, secondEntity.id()));
        assertEquals(Optional.of(secondEntity), cache.getIfPresent(usernameKey, secondEntity.username()));
        assertEquals(Optional.of(secondEntity), cache.getIfPresent(emailKey, secondEntity.email()));
        assertEquals(1L, cache.size());
    }

    @Test
    void invalidateByOneKeyRemovesAllAliases() {
        final MultiKeyLoadingCache.Builder<TestEntity> builder = MultiKeyLoadingCache.builder();
        final MultiKeyLoadingCache.KeySpace<String, TestEntity> idKey =
                builder.addKeySpace("id", TestEntity::id, key -> Optional.empty());
        final MultiKeyLoadingCache.KeySpace<String, TestEntity> usernameKey =
                builder.addKeySpace("username", TestEntity::username, key -> Optional.empty());
        final MultiKeyLoadingCache.KeySpace<String, TestEntity> emailKey =
                builder.addKeySpace("email", TestEntity::email, key -> Optional.empty());
        final MultiKeyLoadingCache<TestEntity> cache = builder.build();
        final TestEntity entity = new TestEntity("1", "alpha", "alpha@example.test");

        cache.put(entity);
        cache.invalidate(usernameKey, entity.username());

        assertEquals(Optional.empty(), cache.getIfPresent(idKey, entity.id()));
        assertEquals(Optional.empty(), cache.getIfPresent(emailKey, entity.email()));
        assertEquals(0L, cache.size());
    }

    @Test
    void cacheDoesNotStoreMisses() {
        final AtomicInteger loadAttempts = new AtomicInteger();
        final MultiKeyLoadingCache.Builder<TestEntity> builder = MultiKeyLoadingCache.builder();
        final MultiKeyLoadingCache.KeySpace<String, TestEntity> idKey =
                builder.addKeySpace("id", TestEntity::id, key -> {
                    loadAttempts.incrementAndGet();
                    return Optional.empty();
                });
        builder.addKeySpace("username", TestEntity::username, key -> Optional.empty());
        final MultiKeyLoadingCache<TestEntity> cache = builder.build();

        assertTrue(cache.get(idKey, "missing").isEmpty());
        assertTrue(cache.get(idKey, "missing").isEmpty());
        assertEquals(2, loadAttempts.get());
        assertEquals(0L, cache.size());
    }

    @Test
    void putRejectsEntitiesWithMissingKeys() {
        final MultiKeyLoadingCache.Builder<TestEntity> builder = MultiKeyLoadingCache.builder();
        builder.addKeySpace("id", TestEntity::id, key -> Optional.empty());
        builder.addKeySpace("username", TestEntity::username, key -> Optional.empty());
        final MultiKeyLoadingCache<TestEntity> cache = builder.build();

        assertThrows(NullPointerException.class,
                () -> cache.put(new TestEntity("1", null, "alpha@example.test")));
    }

    @Test
    void putAllIndexesAllEntitiesAndResolvesConflictsLikePut() {
        final MultiKeyLoadingCache.Builder<TestEntity> builder = MultiKeyLoadingCache.builder();
        final MultiKeyLoadingCache.KeySpace<String, TestEntity> idKey =
                builder.addKeySpace("id", TestEntity::id, key -> Optional.empty());
        final MultiKeyLoadingCache.KeySpace<String, TestEntity> usernameKey =
                builder.addKeySpace("username", TestEntity::username, key -> Optional.empty());
        final MultiKeyLoadingCache.KeySpace<String, TestEntity> emailKey =
                builder.addKeySpace("email", TestEntity::email, key -> Optional.empty());
        final MultiKeyLoadingCache<TestEntity> cache = builder.build();

        final TestEntity firstEntity = new TestEntity("1", "alpha", "alpha@example.test");
        final TestEntity secondEntity = new TestEntity("2", "bravo", "bravo@example.test");
        final TestEntity conflictingEntity = new TestEntity("3", "alpha", "charlie@example.test");

        cache.putAll(List.of(firstEntity, secondEntity, conflictingEntity));

        assertEquals(Optional.empty(), cache.getIfPresent(idKey, firstEntity.id()));
        assertEquals(Optional.of(secondEntity), cache.getIfPresent(idKey, secondEntity.id()));
        assertEquals(Optional.of(conflictingEntity), cache.getIfPresent(idKey, conflictingEntity.id()));
        assertEquals(Optional.of(conflictingEntity), cache.getIfPresent(usernameKey, conflictingEntity.username()));
        assertEquals(Optional.of(conflictingEntity), cache.getIfPresent(emailKey, conflictingEntity.email()));
        assertEquals(Set.of(secondEntity, conflictingEntity), Set.copyOf(cache.getAll()));
        assertEquals(2L, cache.size());
    }

    @Test
    void getAllReturnsUnmodifiableSnapshotOfCachedEntities() {
        final MultiKeyLoadingCache.Builder<TestEntity> builder = MultiKeyLoadingCache.builder();
        builder.addKeySpace("id", TestEntity::id, key -> Optional.empty());
        builder.addKeySpace("username", TestEntity::username, key -> Optional.empty());
        final MultiKeyLoadingCache<TestEntity> cache = builder.build();

        final TestEntity firstEntity = new TestEntity("1", "alpha", "alpha@example.test");
        final TestEntity secondEntity = new TestEntity("2", "bravo", "bravo@example.test");

        cache.putAll(List.of(firstEntity, secondEntity));
        final Set<TestEntity> cachedEntities = Set.copyOf(cache.getAll());

        cache.invalidate(secondEntity);

        assertEquals(Set.of(firstEntity, secondEntity), cachedEntities);
        assertEquals(Set.of(firstEntity), Set.copyOf(cache.getAll()));
        assertThrows(UnsupportedOperationException.class, () -> cache.getAll().add(secondEntity));
    }

    @Test
    void builderRequiresAtLeastTwoKeySpaces() {
        final MultiKeyLoadingCache.Builder<TestEntity> builder = MultiKeyLoadingCache.builder();
        builder.addKeySpace("id", TestEntity::id, key -> Optional.empty());

        assertThrows(IllegalStateException.class, builder::build);
    }

    private record TestEntity(String id, String username, String email) {
    }

    private static final class TestRepository {

        private final Map<String, TestEntity> byId = new ConcurrentHashMap<>();

        private final Map<String, TestEntity> byUsername = new ConcurrentHashMap<>();

        private final Map<String, TestEntity> byEmail = new ConcurrentHashMap<>();

        private TestRepository(final TestEntity entity) {
            byId.put(entity.id(), entity);
            byUsername.put(entity.username(), entity);
            byEmail.put(entity.email(), entity);
        }

        private Optional<TestEntity> findById(final String id) {
            return Optional.ofNullable(byId.get(id));
        }

        private Optional<TestEntity> findByUsername(final String username) {
            return Optional.ofNullable(byUsername.get(username));
        }

        private Optional<TestEntity> findByEmail(final String email) {
            return Optional.ofNullable(byEmail.get(email));
        }
    }
}
