package com.example.legacy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
        List<Todo> found = repository.findAll();
        if (found == null || found.isEmpty()) {
            return Collections.<Todo>emptyList();
        }
        List<Todo> sorted = new ArrayList<Todo>(found);
        Collections.sort(sorted, new Comparator<Todo>() {
            @Override
            public int compare(Todo left, Todo right) {
                Long leftId = left.getId();
                Long rightId = right.getId();
                long leftValue = leftId == null ? Long.MAX_VALUE : leftId.longValue();
                long rightValue = rightId == null ? Long.MAX_VALUE : rightId.longValue();
                return Long.valueOf(leftValue).compareTo(Long.valueOf(rightValue));
            }
        });
        return sorted;
    }

    @Transactional(readOnly = true)
    public List<Todo> findByCompleted(boolean completed) {
        List<Todo> found = repository.findByCompleted(Boolean.valueOf(completed));
        if (found == null) {
            return Collections.<Todo>emptyList();
        }
        return found;
    }

    @Transactional(readOnly = true)
    public List<Todo> search(String fragment) {
        if (fragment == null || fragment.trim().length() == 0) {
            return Collections.<Todo>emptyList();
        }
        return repository.findByTitleContainingIgnoreCaseOrderByIdAsc(fragment.trim());
    }

    @Transactional(readOnly = true)
    public Todo findById(Long id) {
        Optional<Todo> maybe = repository.findById(id);
        if (!maybe.isPresent()) {
            throw new TodoNotFoundException(id);
        }
        return maybe.get();
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
        Todo existing = findById(id);
        existing.setTitle(normalizeTitle(incoming.getTitle()));
        existing.setNotes(trimOrNull(incoming.getNotes()));
        existing.setCompleted(incoming.isCompleted());
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Todo existing = findById(id);
        repository.delete(existing);
    }

    @Transactional(readOnly = true)
    public int countRemaining() {
        List<Todo> all = repository.findAll();
        int remaining = 0;
        for (int i = 0; i < all.size(); i++) {
            Todo todo = all.get(i);
            if (!todo.isCompleted()) {
                Integer boxed = Integer.valueOf(remaining);
                remaining = boxed.intValue() + 1;
            }
        }
        return remaining;
    }

    private String normalizeTitle(String title) {
        if (title == null) {
            return null;
        }
        return title.trim();
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() == 0) {
            return null;
        }
        return trimmed;
    }
}
