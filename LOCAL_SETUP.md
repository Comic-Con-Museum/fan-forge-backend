# Local dev environment setup

If you're interested in getting this up and running as quickly as possible on
your local machine, follow these steps.

>   #### WARNING
>   **This setup is *bad*** for anything more than local deployment. Please
    properly set up and deploy into any remote environments. If you don't know
    how, speak to your local sysadmin. This setup is for developers to have a
    functional backend running as quickly as possible.

0.  When following these instructions, keep track of things in bold. They'll
    be referenced later.
1.  Download and install [PostgreSQL][postgres-dl] for your platform. Find
    your platform in the list, click on its name, and follow the instructions
    there.
2.  Start Postgres. Depending on what you chose -- the EDB installer,
    Postgress.app, etc. -- the exact process will be different, but each has
    good tutorials to guide you through the process. Keep track of **the
    JDBC url** (which looks like `jdbc:postgresql://localhost:5432/postgres`),
    **the database username**, and **the database password**.
3.  Once you've finished installed PostgreSQL, download [Minio][minio-dl]. Do
    the same thing (find your platform, follow the instructions).
4.  Start Minio with the command for your platform on the downloads page. Note
    down **the access key**, **the secret key**, and **the object store URL**,
    each of which is printed to the console on startup by Minio.
5.  [Download the fat JAR][fat-jar] or create it from source as described
    above.
    >   **NOTE**: If you don't see any releases, it means that Nic forgot to
        put one up. You should yell at him to fix it. (And if you do see a
        release but this note is still there, please
        [report a bug][gh-br-tmpl])
6.  Create a file called `application.properties` in the same directory as the
    fat jar. Replace anything in brackets with the value you recorded in
    previous steps.
    ```properties
    spring.datasource.url=[the JDBC url]
    spring.datasource.username=[the database username]
    spring.datasource.password=[the database password]
    
    s3.url=[the object store URL]
    s3.region=us-east-1
    s3.access-key=[the access key]
    s3.secret-key=[the secret key]
    
    security.pwd.secret=asd123!@#lop
    ```
7.  Start the fat jar like any other Java jarfile. You shouldn't need to pass
    any command-line arguments.
8.  Congratulations! You now have a development backend running. If you have
    any issues, see **Troubleshooting** below.

## Troubleshooting

There are a few common errors. If you're having an issue, please make sure it
isn't addressed here before raising an issue.

### `address already in use`

This means that something is already running on the port that the backend is
trying to use. You can either stop the other process, or change the port by
adding this line to `application.properties`:

```properties
server.port=0
```

That randomly chooses a port on startup. Replace `0` with any valid port
number to run on that specific port.

### Something else?

If you're still having trouble, please [ask a question][ask-question]. We'll
try to help you out. Keep in mind that we're all volunteers, though; it may
take some time to get to you. The more relevant details you provide, the less
time it'll take.

 [fat-jar]: https://github.com/Comic-ConMuseum/fan-curation-spring/releases/latest
 [minio-dl]: https://www.minio.io/downloads.html
 [postgres-dl]: https://www.postgresql.org/download
 [ask-question]: https://github.com/Comic-ConMuseum/fan-curation-spring/issues/new?labels=question
