package se.ursamajore.challenge;

public class Result {
    final String url;
    final boolean ok;
    public Result(final String url, final boolean ok) {
        this.url = url;
        this.ok = ok;
    }

    public String getUrl() {
        return url;
    }

    public boolean isOk() {
        return ok;
    }

    @Override
    public String toString() {
        return "Result{" +
                "url='" + url + '\'' +
                ", ok=" + ok +
                '}';
    }
}
