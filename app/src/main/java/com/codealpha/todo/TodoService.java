package com.codealpha.todo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Thread-safe in-memory to-do store. */
public class TodoService {
    public static final int MAX_TITLE = 200;
    private final Map<Long, Todo> todos = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong();

    public Todo create(String title) {
        String t = validate(title);
        long id = seq.incrementAndGet();
        Todo todo = new Todo(id, t, false);
        todos.put(id, todo);
        return todo;
    }

    public List<Todo> list() {
        List<Todo> l = new ArrayList<>(todos.values());
        l.sort(Comparator.comparingLong(Todo::id));
        return l;
    }

    public Optional<Todo> get(long id) { return Optional.ofNullable(todos.get(id)); }

    public Optional<Todo> complete(long id) {
        return Optional.ofNullable(todos.computeIfPresent(id, (k, v) -> new Todo(k, v.title(), true)));
    }

    public boolean delete(long id) { return todos.remove(id) != null; }

    static String validate(String title) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title is required");
        String t = title.strip();
        if (t.length() > MAX_TITLE) throw new IllegalArgumentException("title must be at most " + MAX_TITLE + " characters");
        return t;
    }
}
