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

## Running

To run the backend, you need to have Maven installed and an internet
connection (to download dependencies). Once you have both, navigate into the
directory containing this README and, more importantly, a file called
`pom.xml`. Then run this command:

```
mvn install spring-boot:run
```

This does a full installation, including running unit tests, and then starts
the server. This command **will not** complete -- the server will run in your
terminal until you terminate it with `^C`.

Once you've installed it the first time, you can skip doing an incremental
compilation on nothing by just running this:

```
mvn spring-boot:run
```

However, if you update the source code in any way, you must install again to
see your changes.

## Contributing

On that note, are you interested in helping with... that? There are three main
ways to help:

1. Reporting bugs

    Did you find an issue somewhere in the site? Are things behaving wrong, or
    looking strange, or just not doing what you expect them to? You can submit
    a [bug report][gh-br-template], letting us know what's going wrong. 
    **Please watch the bug report**. We may need more information from you; if
    you disappear, it'll be impossible to fix the issue.

2. Requesting features

    Do you have an idea for something that should be added to the site? You
    should make a [feature request][gh-fr-template] and tell us what it is! We
    can't guarantee that the idea will get in -- even if it fits perfectly
    with the Comic-Con Museum's vision for the site, we might not have the
    developers or other resources to implement it.
    
3. Writing code

    If you're a developer and want to contribute actual code, you can do that,
    too! Look for issues tagged [help wanted][gh-hw-search],
    [good first issue][gh-gfi-search], or [both][gh-hw-gfi-search], and work
    on them. The contribution process isn't very complicated, but it helps us
    make sure no one does work that's already being done and keep everything
    up to the standards we expect. See CONTRIBUTING.md for more information.

 [gh-br-template]: https://github.com/Comic-ConMuseum/fan-curation-spring/issues/new?template=bug-report.md
 [gh-fr-template]: https://github.com/Comic-ConMuseum/fan-curation-spring/issues/new?template=feature_request.md
 [gh-gfi-search]: https://github.com/Comic-ConMuseum/fan-curation-spring/labels/good%20first%20issue
 [gh-hw-search]: https://github.com/Comic-ConMuseum/fan-curation-spring/labels/help%20wanted
 [gh-hw-gfi-search]: https://github.com/Comic-ConMuseum/fan-curation-spring/issues?q=is%3Aopen+label%3A%22good+first+issue%22+label%3A%22help+wanted%22
