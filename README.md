# Comic-Con Museum Fan Curation - Backend

> Note: This was written by one guy with no input from anyone else. If you're
> still seeing this note, it means that there's still no input from anyone
> else, so please take everything you read with a grain of salt.

The Comic-Con Museum's mission is to engage people, whether or not they're
already part of the Comic-Con community, or even the larger comic and movie
fan community. The Democratic Curation website is a large part of that. By
involving fans in the typically very private, very closed-off exhibit curation
process, fans can get engaged in an entirely new way, unique to the Comic-Con
museum: They can suggest, and even help build, the exhibits in the museum.

This repository, specifically, is the backend. It provides the web API which
the frontend uses to get and modify data, which can also be used by
third-party developers to develop apps which integrate with the process.

The code has been made publicly available to encourage fans to get even more
deeply involved, by helping to create the experience they use to create the
experience they get at the Comic-Con Museum.

## Running the server

To run the server, you need, in order:

>   **Note**: *This is not a tutorial.* It's a list of requirements. If you
    want a step-by-step guide to getting a local dev environment running
    on your machine, see [LOCAL_SETUP][local-setup].

1.  [Java][java] 8 or greater.

1.  The [far JAR][fat-jar]. Download this to an empty folder to avoid any
    accidental conflicts.
    
3.  A file named `application.properties` in the same directory as the fat
    JAR.

    ...or any other Spring properties store, but all of our materials assume
    you're using the above. If you want to use some other format, it's on you
    to translate the instructions correctly.

    The format for the file will look something liek this:

    ```properties
    key.one=value of key 1
    key.two=value of key 2
    ```
    
    >   **Note**: This file **must not** be checked into Git.
    
    You need to specify:
    
    *   `security.pwd.secret`: The secret which the password is protected with.
        This ***must*** be kept secret and constant! It can be any random
        sequence of characters, so long as it's secret. Changing this will
        make every user account inaccessible unless the change is reverted.
        FCB makes no attempts to track changes to this file.

4.  A [PostgreSQL][postgres] database.

    There are plans to support other SQL dialects in the future, but for now,
    it's just PostgreSQL.
    
    You need to specify:
    
    *   `spring.datasource.url`: The JDBC URL of the database.
    *   `spring.datasource.username`: The username needed to connect to that
        database.
    *    `spring.datasource.password`: The password for that username.
    
    If you want to use a custom driver, you'll need to specify
    `spring.datasource.driver-class-name` as well. This **is not** necessary
    in most cases, though. If you get an error about being unable to detect
    the correct driver, try setting it to `org.postgresql.Driver`.
    
5.  An S3-API-compatible object store.

    ...which, in practice, means S3. However, [there are other options][minio],
    if you either don't want to use AWS or want to run everything locally. You
    need to provide:
    
    *   `s3.url`: The URL to the S3 server. If no protocol is included, it will
        default to HTTPS.
    *   `s3.region`: The region name that S3 is running in. If you're using a
        separate service, this value will be determined by the service.
    *   `s3.access-key`: The access key. Sometimes called a username.
    *   `s3.secret-key`: The secret key. Sometimes called a password.

Once you have all three set up, just run the fat JAR like any other normal
jarfile:

```
java -jar fcb-fat.jar
```

It will automatically connect to the SQL server and S3 store you've provided.
If it can't reach either, it'll fail fast and tell you what's missing.

### Additional configuration

There are a few more optional configuration options available. In production,
these **must** be left unspecified, as changing them could cause catastrophic
data loss or severely compromise security. They're available only to make
debugging easier.

*   `fcb.reset-on-start`: `true` or `false`; if `true`, the database and
    S3 will be completely cleared when the server starts.
*   `fcb.add-test-data`: `true` or `false`; if `true`, adds a few dozen rows
    of test data to the database on startup.

### Building a fat JAR from source

A fat JAR is just a jarfile which contains in itself all of a Java program's
dependencies. In this project's case, there are a few things required to run,
which are described in **Running** below. However, all of the code can be
packaged into a single Java 9 jar.

Building is easy, as long as you have Maven and an internet connection. Just
run:

```
mvn clean package
```

This automatically downloads dependencies, compiles and packages the jar, and
runs unit tests. The final fat JAR is available in `target/` subdirectory. If
you want to skip the tests, use the flag `-DskipTests=true`; however, it's
heavily recommended that you let them run. It takes longer, but it can catch
errors early. If you do get an error while running unit tests, please report
it as a bug!

The unit tests don't require the other setup described in **Running the
server**. They only require the fat jar.

## API documentation

API documentation is available in [API_DOCS][api-docs].

## Contributing

If you're interested in helping with this project, see
[CONTRIBUTING][contributing].

 [java]: https://www.java.com
 [minio]: https://minio.io/
 [minio-dl]: https://www.minio.io/downloads.html
 [postgres]: https://www.postgresql.org/
 [postgres-dl]: https://www.postgresql.org/download
 [fat-jar]: https://github.com/Comic-ConMuseum/fan-curation-spring/releases/latest
 [local-setup]: LOCAL_SETUP.md
 [api-docs]: API_DOCS.md
 [contributing]: CONTRIBUTING.md
