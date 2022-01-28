package se.ursamajore.challenge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class URLDatabaseImplTest {

    private URLDatabaseImpl database;
    private final List<String> urls = Arrays.asList(
            "https://www.youtube.com/watch?v=vs61OHs2g-w",
            "https://www.youtube.com/watch?v=9HDEHj2yzew"
    );

    Map<String, String> stored = new HashMap<>();


    @BeforeEach
    void init() {
        database = new URLDatabaseImpl("host", 443, true);
        storeUrl();
    }

    @Test
    void search() throws URISyntaxException {
        for (String s : urls) {
            String s1 = stored.get(s);
            URI uri = new URI(s1);
            assertEquals(s, database.search(uri.getPath()).getUrl());
            assertTrue(database.search(uri.getPath()).isOk());
        }
        assertFalse(database.search("https://www.youtube.com/watch?v=k2qgadSvNyU").isOk());
    }

    void storeUrl() {
        for (String s : urls) {
            String storeUrl = database.storeUrl(s);
            stored.put(s, storeUrl);
        }
    }

}