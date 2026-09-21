package com.example.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TodoServiceTest {

    private TodoRepository repository;
    private TodoService service;

    @BeforeEach
    public void setUp() {
        repository = Mockito.mock(TodoRepository.class);
        service = new TodoService(repository);
    }

    @Test
    public void createTrimsTitleAndNotes() {
        when(repository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Todo saved = service.create(new Todo("  write tests  ", "   ", false));

        assertEquals("write tests", saved.getTitle());
        assertEquals(null, saved.getNotes());
    }

    @Test
    public void findByIdThrowsWhenMissing() {
        when(repository.findById(Long.valueOf(42L))).thenReturn(Optional.<Todo>empty());

        assertThrows(TodoNotFoundException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                service.findById(Long.valueOf(42L));
            }
        });
    }

    @Test
    public void findAllReturnsEmptyListWhenNoRows() {
        when(repository.findAll()).thenReturn(Collections.<Todo>emptyList());

        List<Todo> result = service.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    public void findAllSortsById() {
        Todo second = new Todo("second", null, false);
        second.setId(Long.valueOf(2L));
        Todo first = new Todo("first", null, false);
        first.setId(Long.valueOf(1L));
        when(repository.findAll()).thenReturn(new ArrayList<Todo>(Arrays.asList(second, first)));

        List<Todo> result = service.findAll();

        assertEquals("first", result.get(0).getTitle());
        assertEquals("second", result.get(1).getTitle());
    }

    @Test
    public void countRemainingIgnoresCompleted() {
        Todo done = new Todo("done", null, true);
        Todo open = new Todo("open", null, false);
        when(repository.findAll()).thenReturn(new ArrayList<Todo>(Arrays.asList(done, open)));

        assertEquals(1, service.countRemaining());
    }

    @Test
    public void updateReplacesFields() {
        Todo existing = new Todo("old", "old notes", false);
        existing.setId(Long.valueOf(7L));
        when(repository.findById(Long.valueOf(7L))).thenReturn(Optional.of(existing));
        when(repository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Todo updated = service.update(Long.valueOf(7L), new Todo(" new ", " new notes ", true));

        assertEquals("new", updated.getTitle());
        assertEquals("new notes", updated.getNotes());
        assertTrue(updated.isCompleted());
    }

    @Test
    public void searchIgnoresBlankFragment() {
        assertTrue(service.search("   ").isEmpty());
    }
}
