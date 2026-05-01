package com.airtribe.meditrack.interfaces;

import java.util.List;

public interface Searchable<T> {

    T findById(String id);

    List<T> findByName(String name);

    List<T> findByAgeRange(int minAge, int maxAge);


    default List<T> search(String name, int minAge, int maxAge) {
        List<T> byName = findByName(name);
        return byName.isEmpty() ? findByAgeRange(minAge, maxAge) : byName;
    }
}