package se.ursamajore.challenge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void getUrl() {
        final String url = "https://www.youtube.com/watch?v=BC19kwABFwc";
        Result result = new Result(url, true);
        assertEquals(url, result.getUrl());
        assertTrue(result.isOk());
    }

    @Test
    void isOk() {
        Result result = new Result(null, false);
        assertFalse(result.isOk());
        assertNull(result.getUrl());
    }
}