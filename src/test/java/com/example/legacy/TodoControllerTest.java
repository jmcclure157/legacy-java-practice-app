package com.example.legacy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TodoController.class)
public class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService service;

    @Test
    public void listReturnsTodos() throws Exception {
        Todo todo = new Todo("buy milk", null, false);
        todo.setId(Long.valueOf(1L));
        List<Todo> todos = new ArrayList<Todo>(Arrays.asList(todo));
        when(service.findAll()).thenReturn(todos);

        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("buy milk"));
    }

    @Test
    public void getByIdReturnsTodo() throws Exception {
        Todo todo = new Todo("walk dog", "after lunch", false);
        todo.setId(Long.valueOf(5L));
        when(service.findById(Long.valueOf(5L))).thenReturn(todo);

        mockMvc.perform(get("/api/todos/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("walk dog"))
                .andExpect(jsonPath("$.notes").value("after lunch"));
    }

    @Test
    public void getByIdReturnsNotFound() throws Exception {
        when(service.findById(Long.valueOf(99L))).thenThrow(new TodoNotFoundException(Long.valueOf(99L)));

        mockMvc.perform(get("/api/todos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createReturnsCreated() throws Exception {
        Todo saved = new Todo("write code", null, false);
        saved.setId(Long.valueOf(3L));
        when(service.create(any(Todo.class))).thenReturn(saved);

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"write code\",\"completed\":false}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    public void createRejectsBlankTitle() throws Exception {
        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"   \",\"completed\":false}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateReturnsUpdatedTodo() throws Exception {
        Todo updated = new Todo("renamed", null, true);
        updated.setId(Long.valueOf(4L));
        when(service.update(eq(Long.valueOf(4L)), any(Todo.class))).thenReturn(updated);

        mockMvc.perform(put("/api/todos/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"renamed\",\"completed\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    public void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/todos/8"))
                .andExpect(status().isNoContent());

        verify(service).delete(Long.valueOf(8L));
    }

    @Test
    public void deleteMissingReturnsNotFound() throws Exception {
        doThrow(new TodoNotFoundException(Long.valueOf(11L))).when(service).delete(Long.valueOf(11L));

        mockMvc.perform(delete("/api/todos/11"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void remainingReturnsCount() throws Exception {
        when(service.countRemaining()).thenReturn(2);

        mockMvc.perform(get("/api/todos/remaining"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }
}
