package se.ursamajore.challenge;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;


public class URLServer extends AbstractVerticle {
    private final static String CONFIG_FILE = "config.json";
    private URLDatabase database;
    private String host;
    private int port;
    private boolean useSSL = false;
    private HttpServer httpServer;


    /**
     * Reads given resource file as a string.
     *
     * @param fileName the path to the resource file
     * @return the file's contents or null if the file could not be opened
     */
    public String getResourceFileAsString(String fileName) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        if (is != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        return null;
    }

    /**
     * The main start function
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        JsonObject config = loadConfig();
        if (config.isEmpty()) {
            System.out.println("Failed to load configuration from " + CONFIG_FILE);
        } else {
            System.out.println("Configuration: host=" + config().getString("host") + ", port="
                    + config.getInteger("port"));
        }
        host = config.getString("host", "localhost");
        port = config.getInteger("port", 8080);
        database = new URLDatabaseImpl(host, port, useSSL);

        HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setSsl(useSSL);

        httpServer = vertx.createHttpServer(httpServerOptions);
        httpServer.requestHandler(request -> {
                    System.out.println("Got request path=" + request.path() + " method=" + request.method());
                    if (request.path().equals("/") || request.path().equals("/index.html")) {
                        handleBase(request);
                    } else {
                        handleRedirect(request);
                    }
                }
        ).listen(port, host, res -> {
            if (res.failed()) {
                System.err.println("Failed to listen on port " + port + " for host " + host);
                System.exit(1);
            } else {
                System.out.println("Listening on port " + port);
            }
        });
    }

    /**
     * Load the configuration
     * @return Configuration as JSON object
     */
    JsonObject loadConfig() {
        try {
            return new JsonObject(getResourceFileAsString(CONFIG_FILE));
        } catch (Exception e) {
            return new JsonObject();
        }
    }

    /**
     * Handle redirection of a short URL to a longer.
     * Either redirect to the desired URL or return 404 - not found.
     *
     * Try to use html metadata, javascript and fallback to manual redirect of the URL.
     * This will work for all browsers.
     * https://stackoverflow.com/questions/5411538/redirect-from-an-html-page
     *
     * @param request The initial request
     */
    void handleRedirect(HttpServerRequest request) {
        try {
            Result result = database.search(request.path());
            if (result.ok) {
                StringBuffer sb = new StringBuffer();
                sb.append("<!DOCTYPE HTML>")
                        .append("<html lang=\"en-US\">")
                        .append("<head>")
                        .append("<meta charset=\"UTF-8\">")
                        .append("<meta http-equiv=\"refresh\" content=\"0; url=")
                        .append(result.url)
                        .append("\">")
                        .append("<script type=\"text/javascript\">")
                        .append("window.location.href = \"").append(result.url).append("\"")
                        .append("</script>")
                        .append("<title>Page Redirection</title>")
                        .append("</head>")
                        .append("<body>")
                        .append("<!-- Note: don't tell people to `click` the link, just tell them that it is a link. -->")
                        .append("If you are not redirected automatically, follow this <a href='")
                        .append(result.url).append("'>link to ").append(result.url).append("</a>.")
                        .append("</body>")
                        .append("</html>");
                System.out.println("Redirecting "+request.path()+" ==> "+result.url);
                request.response().setStatusCode(301).end(sb.toString());
            } else {
                request.response().setStatusCode(404).end("Invalid URL " + request.path());
            }
        } catch (Exception e) {
            request.response().setStatusCode(500).end("Invalid request, error " + e.getMessage());
        }
    }

    /**
     * Handle base request
     *
     * TODO Add other methods, like delete an entry, authorization etc.
     *
     * @param request The HTTP request
     */
    void handleBase(HttpServerRequest request) {
        HttpMethod method = request.method();
        if (HttpMethod.GET.equals(method)) {
            handleIndex(request);
        } else if (HttpMethod.POST.equals(method)) {
            handlePost(request);
        } else {
            request.response().setStatusCode(401).end("401 - Invalid method " + request.method());
        }
    }

    /**
     * Return a pre-formatted  default page
     * @param request The request to respond to
     */
    void handleIndex(HttpServerRequest request) {
        request
                .response()
                .setStatusCode(200)
                .end(getResourceFileAsString("index.html"));
    }

    /**
     * Upload a new URL
     *
     * @param request The request
     */
    void handlePost(HttpServerRequest request) {
        request.bodyHandler(body -> {
            try {
                String text = body.toString();
                System.out.println("Received JSON: "+text);
                JsonObject json = new JsonObject(text);
                if (!json.containsKey("url")) {
                    request.response().setStatusCode(400).end("Invalid JSON, missing \"url\"");
                }
                String url = json.getString("url");
                String shortUrl = database.storeUrl(url);
                JsonObject jsonResult = new JsonObject();
                jsonResult.put("url", url);
                jsonResult.put("shorturl", shortUrl);
                request
                        .response()
                        .setStatusCode(200)
                        .end(jsonResult.toString());
            } catch (Exception e) {
                e.printStackTrace();
                request.response()
                        .setStatusCode(400)
                        .end( new JsonObject().put("error","Invalid JSON " + e.getMessage()).toString());
            }
        });
        if (!request.isEnded()) {
            request.response()
                    .setStatusCode(400)
                    .end(new JsonObject().put("error","Missing JSON").toString());
        }
    }

    /**
     * For unit testing
     * @param database the database
     */
    void setURLDatabase(URLDatabase database) {
        this.database = database;
    }
}
