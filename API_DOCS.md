# API documentation

A few universal notes:

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

## Authentication

Some endpoints require authentication. The FCB backend uses bearer token
authentication. To get a token, `POST /login` with the login credentials of
the user. To authenticate other requests, add the `Authenticate` header to
the request, in this format:

```
Authentication: Bearer WW91ciB0b2tlbiBoZXJlIQ==
```

Note that the token is an arbitrary string, not necessarily base 64; simply
pass whatever you get from `POST /login`.

Any request over HTTP (as opposed to HTTPS) is treated as unauthenticated, and
attempts to login over HTTP will be rejected.

To log out, `DELETE /login` with an authenticated request.

## `POST /login`

Get a login token to authenticate as a user.

### Request body

```
{
  username: string // The username of the user to auth as.
  password: string // The password of the user to auth as.
}
```

### Authentication

This request must not be authenticated.

## `DELETE /login`

Invalidate the current token.

### Authentication

This request must be authenticated; the authentication token used is what
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

### Authentication

You must be authenticated to hit this URL.

### Request body

The request body is formatted as JSON. If any properties are passed that
aren't specified here, they're ignored. Invalid values will return a 400.

```
{
  title: string // The title or headline of the exhibit
  description: string // A much longer explanation, with details
}
```

### Response body

```
integer
```

Returns the ID of the newly-created exhibit idea.

## `DELETE /exhibit/{id}`

Delete an exhibit by ID.

### Authentication

You must be authenticated as the creator of the exhibit.

## `POST /support/exhibit/{id}`

Mark this exhibit as supported by the current user.

### Authentication

You must be authenticated to hit this endpoint. The user you're authenticated
as is the one which will be recorded as supporting this endpoint.

### Request body

When supporting an exhibit, a survey is presented by the frontend. The request
body contains the survey data. As the specific survey questions haven't been
decided yet, this is taken as a raw string and saved as-is.

## `DELETE /support/exhibit/{id}`

Remove the current user's support for this exhibit

### Authentication

You must be authenticated to hit this endpint. The user you're authenticated
as is the one whose support will be removed.
