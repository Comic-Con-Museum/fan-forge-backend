# API documentation

A few universal notes, for completeness:

*   A `datetime` is a string containing an ISO 8601-formatted date and time.
    These will normally be in UTC, but this **is not** guaranteed, only that
    the datetime will be represented in an ISO 8601-compliant format, and
    contain both date and time information.
*   A `{...}` in a URL indicates a path parameter -- that is, a section of the
    path which can have multiple valid values, each of which does something
    very similar. The valid values are described in that endpoint's "Path
    parameters" section.
*   Some endpoints and properties in normal endpoints are only returned to
    logged-in users. These are noted in the relevant descriptions.
*   If it's not mentioned, it doesn't exist or is ignored. For example, the
    request body to `DELETE /exhibit/:id` is ignored, and therefore it's not
    mentioned in the documentation.

## Response codes

All response codes returned by this server mean what the HTTP standard says
they do. In particular:

Code | Meaning
--- | ---
400 | The request is wrong somehow; the response body gives more details.
401 | The endpoint requires authorization, but the request had none.
403 | You're authorized, but still not allowed to hit that endpoint.
418 | You triggered an easter egg! Good job. Keep it a secret.
500 | Something went wrong while processing the request. File a bug report.

## Authorization

Some endpoints require authorization. The FCB backend uses bearer tokens to
authorize requests.

To get an authorization token, `POST /login` with the login credentials of the
user whose authorization is being used.

To authenticate other requests, add the `Authorization` header to the request,
in this format:

```
Authorization: Bearer WW91ciB0b2tlbiBoZXJlIQ==
```

Under some circumstances, the `Authorization` header may be ignored:

* If the request is made over HTTP, instead of HTTPS.
* If the header is malformed (i.e. anything but bearer authorization)
* If the token is expired, nonexistent, or otherwise invalid

To invalidate a token, `DELETE /login`, authorized with the token to
invalidate.

For more details on the the endpoints' operation, see their documentation.

## `POST /login`

Get a login token to authenticate as a user. If there is no user with the
provided username, a `404 Not Found` is returned. If there is a user with
the username, but the password is invalid, a `401 Unauthorized` is returned.

If the request is made over HTTP, a `400 Bad Request` is returned immediately,
before any other processing is done.

### Request body

```
{
  username: string // The username of the user to auth as.
  password: string // The password of the user to auth as.
}
```

### Response

```
{
  token: string // The new authorization token
  expires: integer // The number of seconds until the token expires.
}
```

>   **Note**: `expires` makes no attempt to account for latency. As a result,
    it may be off by a few seconds, depending on your network connection.

### Authorization

If the request is authorized, no new token will be generated. Instead,
information about the token used will be returned.

## `DELETE /login`

Invalidate the current token.

### Authorization

This request must be authorized; the authorization token used is what
will be invalidated.

## `GET /feed/{type}`

Get a specific feed. The feed returns non-archived exhibits, in an order
determined by the type of feed. See the `{type}` parameter documentation for
more details on specific feeds. Any ties are settled by vote count, then
recency.

Pagination is done by requesting the index in the list of the first exhibit
that you want to be returned. The page size is fixed server-side, and cannot
be specified. This is **not** the ID of the artifact, but the index in the
feed to offset the start of the returned list by.

To reduce response size, the feed doesn't give the full details for an exhibit.
It only gives what's reasonably necessary for a list of summaries. To get more
details about any given exhibit, use `GET /exhibit/{id}`.

### Path parameters

*   `{type}`: The feed to return. This determines what the primary order is.
    Valid values are:
    * `new`: Most recent exhibits first.
    * `alphabetical`: In alphabetical order of titles.

### Query parameters

*   `startIdx` (**required**): The starting index of the list of exhibits to
    return.

### Response

```
{
  startIdx: integer // The starting index which was passed in
  count: integer // The total number of exhibits
  pageSize: integer // The maximum number of exhibits that can be returned
  exhibits: [ // The actual exhibits in the feed at the requested position
    {
      id: integer // The exhibit's unique ID
      title: string // The title or headline of the exhibit
      description: string // A much longer explanation, with details
      supporterCount: integer // How many people have supported the exhibit
      // requires login:
      isSupported: boolean // Whether or not the current user supports it
    }
  ]
}
```

## `GET /exhibit/{id}`

Get more details about a specific exhibit.

If the ID given is invalid, or there's no exhibit with that ID, then this
returns a 404.

### Path parameters

*   `{id}`: The ID of the exhibit. 

### Response

```
{
  id: integer // The exhibit's unique ID
  title: string // The title or headline of the exhibit
  description: string // A much longer explanation, with details
  supporterCount: integer // How many people have supported the exhibit
  author: string // The username of the creator of the exhibit
  created: datetime // When the exhibit was created
  // requires login:
  isSupported: boolean // Whether or not the current user supports it
}
```

## `POST /exhibit`

Create an exhibit with the given details.

### Authorization

You must be authorized to hit this URL.

### Request body

The request body is formatted as JSON. If any properties are passed that
aren't specified here, they're ignored. Invalid values will return a 400.

```
{
  title: string // The title or headline of the exhibit
  description: string // A much longer explanation, with details
}
```

### Response

```
integer // The ID of the newly-created exhibit idea.
```

## `DELETE /exhibit/{id}`

Delete an exhibit by ID.

### Authorization

You must be authorized as the creator of the exhibit.

## `POST /support/exhibit/{id}`

Mark this exhibit as supported by the current user.

### Authorization

You must be authorized to hit this endpoint. The user you're authorized
as is the one which will be recorded as supporting this endpoint.

### Request body

When supporting an exhibit, a survey is presented by the frontend. The request
body contains the survey data. As the specific survey questions haven't been
decided yet, this is taken as a raw string and saved as-is.

## `DELETE /support/exhibit/{id}`

Remove the current user's support for this exhibit

### Authorization

You must be authorized to hit this endpint. The user you're authorized
as is the one whose support will be removed.
