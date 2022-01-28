package se.ursamajore.challenge;



interface URLDatabase {
    Result search(final String shortUrl);
    String storeUrl(final String url);
}
