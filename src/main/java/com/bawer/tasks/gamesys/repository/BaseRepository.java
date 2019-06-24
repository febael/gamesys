package com.bawer.tasks.gamesys.repository;

public interface BaseRepository<T> {

    boolean insert(T obj);
}
