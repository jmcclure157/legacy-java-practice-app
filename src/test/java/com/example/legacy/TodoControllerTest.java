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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TodoController.class)
public class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TodoService service;

    @Test
    public void listReturnsTodos() throws Exception {
        var todo = new Todo("buy milk", null, false);
        todo.setId(1L);
        when(service.findAll()).thenReturn(List.of(todo));

        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("buy milk"));
    }

    @Test
    public void getByIdReturnsTodo() throws Exception {
        var todo = new Todo("walk dog", "after lunch", false);
        todo.setId(5L);
        when(service.findById(5L)).thenReturn(todo);

        mockMvc.perform(get("/api/todos/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("walk dog"))
                .andExpect(jsonPath("$.notes").value("after lunch"));
    }

    @Test
    public void getByIdReturnsNotFound() throws Exception {
        when(service.findById(99L)).thenThrow(new TodoNotFoundException(99L));

        mockMvc.perform(get("/api/todos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createReturnsCreated() throws Exception {
        var saved = new Todo("write code", null, false);
        saved.setId(3L);
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
        var updated = new Todo("renamed", null, true);
        updated.setId(4L);
        when(service.update(eq(4L), any(Todo.class))).thenReturn(updated);

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

        verify(service).delete(8L);
    }

    @Test
    public void deleteMissingReturnsNotFound() throws Exception {
        doThrow(new TodoNotFoundException(11L)).when(service).delete(11L);

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
