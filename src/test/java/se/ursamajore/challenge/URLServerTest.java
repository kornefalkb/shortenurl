package se.ursamajore.challenge;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import org.mockito.ArgumentMatcher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class URLServerTest {

    @org.junit.jupiter.api.Test
    void getResourceFileAsString() {
    }

    @org.junit.jupiter.api.Test
    void loadConfig() {
        URLServer urlServer = new URLServer();
        JsonObject entries = urlServer.loadConfig();
        assertEquals("localhost", entries.getString("host"));
        assertEquals(8080, entries.getInteger("port"));
    }

    @org.junit.jupiter.api.Test
    void handleRedirect() {
        final String shortCut = "kK73m";
        final String url = "https://mvnrepository.com/artifact/org.mockito/mockito-junit-jupiter/4.3.1";
        URLServer urlServer = new URLServer();
        URLDatabase database = mock(URLDatabase.class);
        urlServer.setURLDatabase(database);

        HttpServerRequest request = mock(HttpServerRequest.class);
        when(request.path()).thenReturn(shortCut);
        when(database.search(shortCut)).thenReturn(new Result(url, true));
        HttpServerResponse response = mock(HttpServerResponse.class);
        when(request.response()).thenReturn(response);
        when(response.setStatusCode(anyInt())).thenReturn(response);
        urlServer.handleRedirect(request);
        ArgumentMatcher<String> matcher = new ArgumentMatcher<String>() {
            @Override
            public boolean matches(String s) {
                return s.contains(url);
            }
        };
        verify(response, times(1)).setStatusCode(301);
        verify(response, times(1)).end(argThat(matcher));
    }

    @org.junit.jupiter.api.Test
    void handleBase() {
        URLServer urlServer = new URLServer();
        URLDatabase database = mock(URLDatabase.class);
        when(database.search(anyString())).thenReturn(new Result("", false));

        HttpServerRequest request = mock(HttpServerRequest.class);
        when(request.path()).thenReturn("shortCut");
        when(request.method()).thenReturn(HttpMethod.GET).thenReturn(HttpMethod.POST).thenReturn(HttpMethod.PUT);

        HttpServerResponse response = mock(HttpServerResponse.class);
        when(request.response()).thenReturn(response);
        when(response.setStatusCode(anyInt())).thenAnswer(a -> {
            Integer argument = a.getArgument(0, Integer.class);
            return response;
        });
        urlServer.handleBase(request);
        urlServer.handleBase(request);
        urlServer.handleBase(request);
        verify(response, times(1)).setStatusCode(200);
        verify(response, times(1)).setStatusCode(401);
        verify(response, times(1)).setStatusCode(400);
    }

    @org.junit.jupiter.api.Test
    void handleIndex() {
        URLServer urlServer = new URLServer();
        HttpServerRequest request = mock(HttpServerRequest.class);
        HttpServerResponse response = mock(HttpServerResponse.class);
        when(request.response()).thenReturn(response);
        when(response.setStatusCode(anyInt())).thenAnswer(a -> {
            Integer argument = a.getArgument(0, Integer.class);
            return response;
        });
        ArgumentMatcher<String> matcher = new ArgumentMatcher<String>() {
            @Override
            public boolean matches(String s) {
                return s.contains("<html>") &&
                        s.contains("</html>") &&
                        s.startsWith("<") && s.endsWith(">");
            }
        };
        urlServer.handleIndex(request);
        verify(response, times(1)).setStatusCode(200);
        verify(response, times(1)).end(argThat(matcher));

    }

    @org.junit.jupiter.api.Test
    void handlePost() {
        final String url = "https://www.baeldung.com/mockito-argument-matchers";
        final String shortUrl = "aBn6";
        URLServer urlServer = new URLServer();
        URLDatabase database = mock(URLDatabase.class);
        when(database.storeUrl(url)).thenAnswer(a -> {
            return shortUrl;
        });
        urlServer.setURLDatabase(database);

        HttpServerRequest request = mock(HttpServerRequest.class);
        when(request.path()).thenReturn("shortCut");
        when(request.method()).thenReturn(HttpMethod.GET).thenReturn(HttpMethod.POST).thenReturn(HttpMethod.PUT);

        HttpServerResponse response = mock(HttpServerResponse.class);
        when(request.response()).thenReturn(response);
        when(response.setStatusCode(anyInt())).thenAnswer(a -> {
            Integer argument = a.getArgument(0, Integer.class);
            return response;
        });
        when(request.bodyHandler(any()))
                .thenAnswer(a -> {
                    @SuppressWarnings("unchecked")
                    Handler<Buffer> bodyHandler = (Handler<Buffer>)a.getArgument(0, Handler.class);
                    Buffer buffer = mock(Buffer.class);
                    JsonObject json = new JsonObject();
                    json.put("url", url);
                    when(buffer.toString()).thenReturn(json.toString());
                    bodyHandler.handle(buffer);
                    return request;
                });


        urlServer.handlePost(request);
        ArgumentMatcher<String> matcher = new ArgumentMatcher<String>() {
            @Override
            public boolean matches(String s) {
                return s.contains("\"" + url + "\"") &&
                        s.contains("\"" + shortUrl + "\"") &&
                        s.startsWith("{") && s.endsWith("}");
            }
        };
        verify(response, times(1)).setStatusCode(200);
        verify(response, times(1)).end(argThat(matcher));
    }
}