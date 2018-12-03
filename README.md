# Comic-Con Museum Fan Forge - Backend
################################################################################
This repository is the backend for the Comic-Con Museum fan curation website,
Fan Forge. It provides the web API which the frontend uses to get and modify
data. It can also be used by third-party developers to develop apps which
integrate with the process.

The Fan Forge website allows the Comic-Con Museum to engage with new and
established fans in exciting ways, using upvotes, downvotes, and surveys to get
the public involved in exhibit development and curation. 

The code that powers Fan Forge is publically available to get fans to even more
deeply involved, by helping to create the experience they have while creating
their museum experience. Read through the sections below to get started.

## API documentation

If you're interested in integration with Fan Forge, you'll want to look at
[API_DOCS][api-docs]. It details how to use each of the endpoints.

## Contributing

If you're interested in helping with this project, see
[CONTRIBUTING][contributing]. You don't need to be a developer to contribute!

## Running the server

>   **Note**: *This is not a tutorial.* It's a list of requirements. If you
    want a step-by-step guide to getting a local dev environment running
    on your machine, see [LOCAL_SETUP][local-setup].

To run the server, you need, in order:

 1. [Java][java] 8 or greater.

 2. The fat JAR, available [here][fat-jar]. Put this in an empty folder to
    avoid any accidental conflicts.
    
 3. A file named `application.properties` in the same directory as the fat
    JAR.

    ...or any other Spring properties store, but all of our materials assume
    you're using the above. If you want to use some other format, it's on you
    to translate the instructions correctly.

    The format for the file will look something like this:

    ```properties
    key.one=value of key 1
    key.two=value of key 2
    ```
    
    >   **Note**: This file **must not** be checked into Git.
    
    You need to specify:
    
     *  `security.pwd.secret`: The secret which the password is protected with.
        This ***must*** be kept secret and constant! It can be any random
        sequence of characters, so long as it's secret. Changing this will
        make every user account inaccessible unless the change is reverted.
        The Fan Forge backend makes no attempts to track changes to this file
        by design.

 4. A [PostgreSQL][postgres] database. There are plans to support other SQL
    dialects in the future, but for now, it's just PostgreSQL.
    
    You need to specify:
    
     *  `spring.datasource.url`: The JDBC URL of the database.
     *  `spring.datasource.username`: The username needed to connect to that
        database.
     *  `spring.datasource.password`: The password for that username.
    
    You might not need to supply the URL and the username/password. Some JDBC
    URLs already have that information provided. If you're not sure, try
    leaving it off -- if everything works as expected, it's unnecessary.
    
    If you want to use a custom driver, you'll need to specify
    `spring.datasource.driver-class-name` as well. This **is not** necessary
    in most cases, though. If you get an error about being unable to detect
    the correct driver, try setting it to `org.postgresql.Driver`.
    
 5. An S3-API-compatible object store.

    ...which, in practice, means S3. However, [there are other options][minio],
    if you either don't want to use AWS or want to run everything locally. You
    need to provide:
    
     *  `s3.url`: The URL to the S3 server. If no protocol is included, it will
        default to HTTPS.
     *  `s3.region`: The region name that S3 is running in. If you're using a
        separate service, this value will be determined by the service.
     *  `s3.access-key`: The access key. Sometimes called a username.
     *  `s3.secret-key`: The secret key. Sometimes called a password.
     *  `s3.bucket`: The name of the bucket to access.

Once you have all three set up, just run the fat JAR like any other normal
jarfile:

```
java -jar fan-forge-backend.jar
```

It will automatically connect to the SQL server and S3 store you've provided.
If it can't reach either, it'll fail fast and tell you what's missing.

### Additional configuration

There are a few more optional configuration options available.

 *  `spring.servlet.multipart.max-file-size`: The maximum size of an individual
    image in an upload. Defaults to 64 kilobytes.
 *  `spring.servlet.multipart.max-request-size`: The maximum total size of a
    request that's uploading files. Defaults to 512 kilobytes.

There are also some debug options. In production, these **must** be left
unspecified, as changing them could cause catastrophic data loss, massive
instability, or severely compromise security. They're available **only** to
make debugging easier. **Do not set them to any value** in production.

 *  `ff.reset-on-start`: Whether or not the database and S3 will be
    completely reset when the server starts. This ensures that the database
    stays up-to-date through frequent code changes.
 *  `ff.add-test-data`: Whether or not to add a few dozen rows of test data
    to the database on startup. This ensures that there's always data to test
    against, even if the persistence is being reset with every restart.
 *  `ff.close-on-init-fail`: Choose if a failure during initialization should
    lead to a crash on startup.
 *  `ff.require-https`: Enable or disable the HTTPS requirement for
    authentication.

### Building a fat JAR from source

A fat JAR is just a jarfile which contains in itself all of a Java program's
dependencies. In this project's case, there are a few things required to run,
which are described in **Running the server** above. However, all of the code
can be packaged into a single Java 8 JAR.

Building is easy, as long as you have Maven and an internet connection. In the
root directory of the repo, run:

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

 [java]: https://www.java.com
 [minio]: https://minio.io/
 [minio-dl]: https://www.minio.io/downloads.html
 [postgres]: https://www.postgresql.org/
 [postgres-dl]: https://www.postgresql.org/download
 [fat-jar]: https://github.com/Comic-ConMuseum/fan-curation-spring/releases/latest
 [local-setup]: LOCAL_SETUP.md
 [api-docs]: API_DOCS.md
 [contributing]: CONTRIBUTING.md
