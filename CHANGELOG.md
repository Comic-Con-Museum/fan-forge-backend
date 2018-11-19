# Changelog

## 0.9.1

HTTPS is now required for a request to be considered authorized at all. If the
request is HTTP, the value of the `Authorizaton` header is ignored.

Require HTTPS for a request to be considered authenticated. This behavior can
be disabled by setting `ff.require-https` to `false`.
