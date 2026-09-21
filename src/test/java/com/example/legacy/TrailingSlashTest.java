package com.example.legacy;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TodoController.class)
@Import(WebConfig.class)
public class TrailingSlashTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TodoService service;

    @Test
    public void listMatchesWithTrailingSlash() throws Exception {
        Todo todo = new Todo("buy milk", null, false);
        todo.setId(1L);
        when(service.findAll()).thenReturn(List.of(todo));

        mockMvc.perform(get("/api/todos/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("buy milk"));
    }

    @Test
    public void listMatchesWithoutTrailingSlash() throws Exception {
        when(service.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk());
    }

    @Test
    public void itemMatchesWithTrailingSlash() throws Exception {
        Todo todo = new Todo("walk dog", null, false);
        todo.setId(5L);
        when(service.findById(5L)).thenReturn(todo);

        mockMvc.perform(get("/api/todos/5/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("walk dog"));
    }
}
