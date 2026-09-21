package com.example.legacy;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TodoService {

    private final TodoRepository repository;

    public TodoService(TodoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Todo> findAll() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Todo::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Todo> findByCompleted(boolean completed) {
        return repository.findByCompleted(completed);
    }

    @Transactional(readOnly = true)
    public List<Todo> search(String fragment) {
        if (fragment == null || fragment.isBlank()) {
            return List.of();
        }
        return repository.findByTitleContainingIgnoreCaseOrderByIdAsc(fragment.strip());
    }

    @Transactional(readOnly = true)
    public Todo findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new TodoNotFoundException(id));
    }

    @Transactional
    public Todo create(Todo todo) {
        todo.setId(null);
        todo.setTitle(normalizeTitle(todo.getTitle()));
        todo.setNotes(trimOrNull(todo.getNotes()));
        return repository.save(todo);
    }

    @Transactional
    public Todo update(Long id, Todo incoming) {
        var existing = findById(id);
        existing.setTitle(normalizeTitle(incoming.getTitle()));
        existing.setNotes(trimOrNull(incoming.getNotes()));
        existing.setCompleted(incoming.isCompleted());
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(findById(id));
    }

    @Transactional(readOnly = true)
    public int countRemaining() {
        return (int) repository.findAll().stream()
                .filter(todo -> !todo.isCompleted())
                .count();
    }

    private String normalizeTitle(String title) {
        return title == null ? null : title.strip();
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        var trimmed = value.strip();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
