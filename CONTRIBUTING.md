# Contributing

There are three main ways to contribute: Bug reports, feature requests, and
pull requests. Feel free to make use of any or all of them, but please keep in
mind that the developers are all volunteers. You may get reponses slowly, or
not at all.

If you're looking to contribute something other than code, this repo may not be
the best place. All contributions are welcome, but the [frontend][frontend] is
more likely to have things you can help with.

## Bug reports

Did you find an issue somewhere in the site? Are things behaving wrong, or
looking strange, or just not doing what you expect them to? You can submit a
[bug report][gh-br-tmpl], letting us know what's going wrong.  **Please
"subscribe" to the bug report**, with the button on the right. We may need
more information from you! If you disappear, it'll be impossible to fix the
issue, and we'll close the ticket. Please also let us know if the issue fixes
itself -- we can look at what changed and try to track it down.

## Feature requests

Do you have an idea for something that should be added to the site? You should
make a [feature request][gh-fr-tmpl] and tell us what it is! We can't
guarantee that the idea will get in -- even if it fits perfectly with the
Comic-Con Museum's vision for the site, we might not have the developers or
other resources to implement it. Once you've proposed a feature request, we'll
discuss it, and proceed from there.

## Pull requests

If you know your way around code and want to help with that, you can absolutely
do that too. You'll need a good working knowledge of Git

 1. Find an issue that no one else has claimed. That means anything tagged
    [up for grabs][gh-ufg], but if it's your first PR, you might also want
    to look at [good first issues][gh-gfi].
 2. Comment on the issue's thread to claim it. Wait for a maintainer to say
    that the issue is yours before continuing.
 3. Fork the repo with the button in the top right.
 4. Create a new branch starting with the number of your issue -- for example
    `29-add-hello-world`.
 5. Make your changes on that branch. Be sure to add integration and unit tests
    as necessary to cover your new code! Do manual tests, too, to double-check.
    Any PR that changes code without updating the tests will be rejected, _even
    if no tests break_, unless you can give a very good reason as to why.
 6. When you're done with your code, open a pull request. Follow the format! If
    your commit makes any changes to the database, include a minimal SQL script
    to migrate the database. Be descriptive but concise in the changelog section.
    Make sure you include `Resolves #n`, referencing the issue that the PR is
    meant to close.
 7. The contributors and maintainers might have some changes for you to make.
    Please make them promptly -- it helps speed everything up if we're not
    left waiting days for changes.
 8. Once everything looks good, your PR will be accepted, and the code will
    be automatically merged in and added to the next release. Congrats!

Note that maintainers are (partially) exempt from this workflow -- they're the
ones doing hotfixes, and know what rules they can break and when. Don't feel
like you can't call one out on violating this procedure, but don't get upset if
they violate it anyway.

 [gh-br-tmpl]: https://github.com/Comic-ConMuseum/fan-curation-spring/issues/new?template=bug-report.md
 [gh-fr-tmpl]: https://github.com/Comic-ConMuseum/fan-curation-spring/issues/new?template=feature_request.md
 [gh-ufg]: https://github.com/Comic-ConMuseum/fan-curation-spring/issues?q=is%3Aissue+is%3Aopen+label%3A%22up+for+grabs%22
 [gh-gfi]: https://github.com/Comic-ConMuseum/fan-curation-spring/labels/good%20first%20issue
 [gh-hw]: https://github.com/Comic-ConMuseum/fan-curation-spring/labels/help%20wanted
 [ccs]: https://www.conventionalcommits.org/en/v1.0.0-beta.2/#specification
 [frontend]: https://github.com/Comic-Con-Museum/fan-forge-frontend
 
