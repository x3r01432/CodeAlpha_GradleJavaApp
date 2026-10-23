package com.codealpha.todo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TodoServiceTest {
    @Test void createsAndLists() {
        TodoService s = new TodoService();
        s.create("a"); s.create("  b  ");
        assertEquals(2, s.list().size());
        assertEquals("b", s.list().get(1).title());
    }
    @Test void completesAndDeletes() {
        TodoService s = new TodoService();
        long id = s.create("x").id();
        assertTrue(s.complete(id).orElseThrow().done());
        assertTrue(s.delete(id));
        assertFalse(s.delete(id));
        assertTrue(s.complete(99).isEmpty());
    }
    @Test void rejectsBadTitles() {
        TodoService s = new TodoService();
        assertThrows(IllegalArgumentException.class, () -> s.create(" "));
        assertThrows(IllegalArgumentException.class, () -> s.create(null));
        assertThrows(IllegalArgumentException.class, () -> s.create("x".repeat(201)));
    }
}
