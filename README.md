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

The preferred way to run the backend is with a fat JAR. These are the `.jar`
files attached to releases, and make up the entirety of the actual code that
runs the backend. The JAR can also be built from source, if you want to use
a specific version, or test local changes before making a PR.

Once the JAR is correctly set up, three more things are needed:

*   An `application.properties` file.

    This is how both Spring and the backend are configured. The file should
    be located in the same directory as the fat JAR, and called exactly
    `application.properties`. Its format looks like this:
    
    ```properties
    key.one=value of key 1
    key.two=value of key 2
    ```
    
    Because the backend only reads properties through Spring, any format
    and location that Spring supports will work here, too. Explaining all of
    that is outside the scope of this guide, though.
    
    This file **must not** be checked into Git.
    
    You need to specify:
    *   `security.pwd.secret`: The secret which the password is protected with.
        This ***must*** be kept secret and constant! It can be any random
        sequence of characters, so long as it's secret. Changing this will
        invalidate all of the passwords in the database, and the application
        intentionally does **not** attempt to detect changes to this property.

*   A SQL database.

    [PostgreSQL][postgres] is open-source and available for every major
    platform, and can be run locally, and is what the default properties are
    set up to expect.
    
    You need to specify the SQL server to connect to, so set the following
    properties in `application.properties`:
    
    *   `spring.datasource.url`: The URL of the database.
    *   `spring.datasource.username`: The username needed to connect to that
        database.
    *    `spring.datasource.password`: The password for that username.
    
    There's also `spring.datasource.driver-class-name`. This should *not* need
    to be set, but if you want to use a custom driver or encounter issues
    about an appropriate driver class not being found, it may need to be
    specified.

*   An S3-API-compatible object store.

    ...which, in practice, means S3. However, [there are other options][minio],
    if you either don't want to use AWS or want to run everything locally. You
    need to provide:
    
    *   `s3.url`: The URL to the S3 server. If no protocol is included, it will
        default to HTTPS.
    *   `s3.region`: The region name that S3 is running in. If you're using a
        separate service, the value to put here will depend on that.
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
*   `security.pwd.hash-strength`: How strong the password protection should
    be. The higher the number, the safer the passwords, but the more time
    authentication will take. If set to 0, no hashing will be used.
*   

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
server**. They only require 

## API documentation

API documentation is available in API_DOCS.md.

## Contributing

If you're interested in helping with this project, there are three main ways.

### Bug reports

Did you find an issue somewhere in the site? Are things behaving wrong, or
looking strange, or just not doing what you expect them to? You can submit a
[bug report][gh-br-template], letting us know what's going wrong.  **Please
"subscribe" to the bug report**, with the button on the right. We may need
more information from you; if you disappear, it'll be impossible to fix the
issue, and we'll close the issue. Please also let us know if the issue fixes
itself -- we can look at what changed and try to track it down.

### Feature requests

Do you have an idea for something that should be added to the site? You should
make a [feature request][gh-fr-template] and tell us what it is! We can't
guarantee that the idea will get in -- even if it fits perfectly with the
Comic-Con Museum's vision for the site, we might not have the developers or
other resources to implement it.
    
### Pull requests

If you're a software developer and want to contribute actual code, you can do
that, too! Look for issues tagged [help wanted][gh-hw-search],
[good first issue][gh-gfi-search], or [both][gh-hw-gfi-search], and work on
them. The contribution process isn't very complicated, but it helps us make
sure no one does work that's already being done and keep everything up to the
standards we expect. See CONTRIBUTING.md for more information.

 [gh-br-template]: https://github.com/Comic-ConMuseum/fan-curation-spring/issues/new?template=bug-report.md
 [gh-fr-template]: https://github.com/Comic-ConMuseum/fan-curation-spring/issues/new?template=feature_request.md
 [gh-gfi-search]: https://github.com/Comic-ConMuseum/fan-curation-spring/labels/good%20first%20issue
 [gh-hw-search]: https://github.com/Comic-ConMuseum/fan-curation-spring/labels/help%20wanted
 [gh-hw-gfi-search]: https://github.com/Comic-ConMuseum/fan-curation-spring/issues?q=is%3Aopen+label%3A%22good+first+issue%22+label%3A%22help+wanted%22
 [minio]: https://minio.io/
