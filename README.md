# Short URL demo
A demo program showing the principles of create and store
a URL to any server address and retreive a short URL.

## Building
Use maven to build the project
`mvn clean install`

## Running
From command line `java -jar target/shortenurl-fat.jar`

## Adding a URL
```bash
curl --header "Content-Type: application/json" \
        --request POST \
        --data '{ "url": "https://www.cabonline.com/resa" }' \
        http://localhost:8080/
```

## Test it
Running the curl command as shown gives the JSON formatted URL
back as
```json
{
   "url" : "https://www.cabonline.com/resa",
   "shorturl" : "http://localhost:8080/ru"
}
```

Start your web browser and navigate to http://localhost:8080/ru
and you should be redirected to the longer url https://www.cabonline.com/resa


### Notes
- The port number is omitted if using the standard ports 80 or 443 (https).
- Currently no HTTPS functionality is added
- The database is only in-memory for simplicity
- The number of stored url:s depends of the database
capabilities and a maximum size of 2^64 entries   