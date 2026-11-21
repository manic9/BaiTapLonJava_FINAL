package com.esports.manager.repository;

import com.esports.manager.model.Identifiable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRepository<T extends Identifiable> implements Repository<T> {

    private final ObservableList<T> items = FXCollections.observableArrayList();
    private final Map<String, T> index = new ConcurrentHashMap<>();

    @Override
    public ObservableList<T> findAll() {
        return items;
    }

    @Override
    public Optional<T> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(index.get(id));
    }

    @Override
    public T save(T entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("Entity hoặc ID không được để trống");
        }
        if (index.containsKey(entity.getId())) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getId().equals(entity.getId())) {
                    items.set(i, entity);
                    break;
                }
            }
        } else {
            items.add(entity);
        }
        index.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null) return false;
        T removed = index.remove(id);
        if (removed != null) {
            items.removeIf(item -> item.getId().equals(id));
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        index.clear();
        items.clear();
    }

    @Override
    public int count() {
        return items.size();
    }
}