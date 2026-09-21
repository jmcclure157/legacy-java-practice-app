package com.example.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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

        var saved = service.create(new Todo("  write tests  ", "   ", false));

        assertEquals("write tests", saved.getTitle());
        assertEquals(null, saved.getNotes());
    }

    @Test
    public void findByIdThrowsWhenMissing() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(TodoNotFoundException.class, () -> service.findById(42L));
    }

    @Test
    public void findAllReturnsEmptyListWhenNoRows() {
        when(repository.findAll()).thenReturn(List.of());

        assertTrue(service.findAll().isEmpty());
    }

    @Test
    public void findAllSortsById() {
        var second = new Todo("second", null, false);
        second.setId(2L);
        var first = new Todo("first", null, false);
        first.setId(1L);
        when(repository.findAll()).thenReturn(List.of(second, first));

        var result = service.findAll();

        assertEquals("first", result.get(0).getTitle());
        assertEquals("second", result.get(1).getTitle());
    }

    @Test
    public void countRemainingIgnoresCompleted() {
        when(repository.findAll()).thenReturn(List.of(new Todo("done", null, true), new Todo("open", null, false)));

        assertEquals(1, service.countRemaining());
    }

    @Test
    public void updateReplacesFields() {
        var existing = new Todo("old", "old notes", false);
        existing.setId(7L);
        when(repository.findById(7L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = service.update(7L, new Todo(" new ", " new notes ", true));

        assertEquals("new", updated.getTitle());
        assertEquals("new notes", updated.getNotes());
        assertTrue(updated.isCompleted());
    }

    @Test
    public void searchIgnoresBlankFragment() {
        assertTrue(service.search("   ").isEmpty());
    }
}
