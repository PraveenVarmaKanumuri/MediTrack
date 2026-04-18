package com.airtribe.meditrack.interfaces;

import java.util.List;

public interface Searchable<T> {

    T findById(String id);

    List<T> findByName(String name);

    List<T> findByAgeRange(int minAge, int maxAge);
}