package com.example.legacy;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Todo> list(@RequestParam(name = "completed", required = false) Boolean completed,
                           @RequestParam(name = "q", required = false) String query) {
        if (completed != null) {
            return service.findByCompleted(completed.booleanValue());
        }
        if (query != null) {
            return service.search(query);
        }
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Todo get(@PathVariable("id") Long id) {
        return service.findById(id);
    }

    @GetMapping("/remaining")
    public Integer remaining() {
        return Integer.valueOf(service.countRemaining());
    }

    @PostMapping
    public ResponseEntity<Todo> create(@Valid @RequestBody Todo todo) {
        Todo saved = service.create(todo);
        return new ResponseEntity<Todo>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public Todo update(@PathVariable("id") Long id, @Valid @RequestBody Todo todo) {
        return service.update(id, todo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }
}
