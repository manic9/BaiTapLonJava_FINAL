package com.esports.manager.repository;

import com.esports.manager.model.Identifiable;
import javafx.collections.ObservableList;

import java.util.Optional;

public interface Repository<T extends Identifiable> {
    ObservableList<T> findAll();
    Optional<T> findById(String id);
    T save(T entity);
    boolean deleteById(String id);
    void clear();
    int count();
}