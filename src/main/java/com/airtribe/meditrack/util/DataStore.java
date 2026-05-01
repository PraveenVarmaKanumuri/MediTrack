package com.airtribe.meditrack.util;

import com.airtribe.meditrack.exception.InvalidDataException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DataStore<T> {

    private final Map<String, T> store;
    private final String entityName;

    public DataStore(String entityName) {
        this.store = new HashMap<>();
        this.entityName = entityName;
    }

    public void save(String id, T entity) {
        if (id == null || id.isBlank()) {
            throw new InvalidDataException("id", "cannot be null or empty");
        }
        if (entity == null) {
            throw new InvalidDataException(entityName, "entity cannot be null");
        }
        store.put(id, entity);
    }

    public Optional<T> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(id));
    }

    public List<T> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }

    public boolean existsById(String id) {
        return store.containsKey(id);
    }

    public void delete(String id) {
        if (id == null || id.isBlank()) {
            throw new InvalidDataException("id", "cannot be null or empty");
        }
        if (!store.containsKey(id)) {
            throw new InvalidDataException(entityName,
                    String.format("No %s found with id: %s", entityName, id));
        }
        store.remove(id);
    }

    public int count() {
        return store.size();
    }

    public boolean isEmpty() {
        return store.isEmpty();
    }
}