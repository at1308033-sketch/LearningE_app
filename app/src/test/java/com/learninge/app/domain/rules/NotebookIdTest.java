package com.learninge.app.domain.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import java.util.UUID;

public class NotebookIdTest {
    private static final String USER = "3f2b8c1e-5a4d-4e6f-9a7b-1c2d3e4f5a6b";

    @Test
    public void of_isDeterministicVersion5() {
        // Giá trị đối chiếu tính bằng uuid.uuid5 của Python, cùng thuật toán với server.
        assertEquals("c72a987c-3a2b-57bb-b721-720649f3edb4", NotebookId.of(USER));
        assertEquals(5, UUID.fromString(NotebookId.of(USER)).version());
    }

    @Test
    public void of_differsByUser() {
        assertNotEquals(NotebookId.of(USER), NotebookId.of(USER.replace('3', '4')));
    }
}
