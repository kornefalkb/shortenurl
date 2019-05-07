package se.ursamajore.challenge;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple in-memory URL storage
 */
public class URLDatabaseImpl implements URLDatabase {
    private final Map<String, String> db = new HashMap<>();
    private final String host;
    private final int port;
    private final boolean useSSL;
    private AtomicLong atomicLong = new AtomicLong(1001);
    private final String homePath;

    public URLDatabaseImpl(final String host, final int port, final boolean useSSL) {
        this.host = host;
        this.port = port;
        this.useSSL = useSSL;
        homePath = createHomePath();
        System.out.println("Database started, homePath="+homePath);
    }

    @Override
    public Result search(String shortUrl) {
        if (db.containsKey(shortUrl)) {
            return new Result(db.get(shortUrl), true);
        }
        return new Result("", false);
    }

    @Override
    public String storeUrl(String url) {
        String shortUrl = createNewShortUrl();
        db.put(shortUrl, url);
        return homePath + shortUrl;
    }

    private String createNewShortUrl() {
        StringBuffer sb = new StringBuffer();
        sb
                .append("/")
                .append(Long.toString(atomicLong.incrementAndGet(), 36));
        return sb.toString();
    }

    private String createHomePath() {
        StringBuffer sb = new StringBuffer();
        if (useSSL) {
            sb.append("https://");
        } else {
            sb.append("http://");
        }
        sb
                .append(host);
        if ((port != 80) && !useSSL || (port != 443) && useSSL) {
            sb
                    .append(":")
                    .append(port);
        }
        return sb.toString();
    }

}
